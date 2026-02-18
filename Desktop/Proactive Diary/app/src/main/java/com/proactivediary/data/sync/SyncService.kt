package com.proactivediary.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.WritingReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core cloud sync engine. Handles pushing local Room data to Firestore
 * and is the foundation for the restore flow.
 *
 * Design: Room is the source of truth. Firestore is the backup.
 * All writes go to Room first, then fire-and-forget to Firestore.
 */
@Singleton
class SyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val entryDao: EntryDao,
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao,
    private val reminderDao: WritingReminderDao,
    private val preferenceDao: PreferenceDao,
    private val analyticsService: AnalyticsService
) {
    companion object {
        private const val TAG = "SyncService"

        // Firestore subcollection names under users/{uid}/
        const val ENTRIES = "entries"
        const val GOALS = "goals"
        const val GOAL_CHECKINS = "goal_checkins"
        const val REMINDERS = "reminders"
        const val PREFERENCES = "preferences"
        const val SYNC_META = "sync_meta"

        // Preference keys that should sync to cloud (design/theme prefs only)
        val SYNCABLE_PREF_KEYS = setOf(
            "diary_color", "diary_form", "diary_texture", "diary_canvas",
            "diary_details", "diary_mark_text", "diary_mark_position", "diary_mark_font",
            "design_completed", "design_studio_completed", "font_size"
        )
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val uid: String? get() = auth.currentUser?.uid

    /** Base path for the current user's sync data */
    private fun userCollection(collection: String): CollectionReference? {
        val currentUid = uid ?: return null
        return firestore.collection("users").document(currentUid).collection(collection)
    }

    // ═══════════════════════════════════════════════
    // PUSH: Single entity writes (fire-and-forget)
    // ═══════════════════════════════════════════════

    /** Push a single entry to Firestore. Called after Room insert/update. */
    suspend fun pushEntry(entry: EntryEntity) {
        val col = userCollection(ENTRIES) ?: return
        try {
            col.document(entry.id)
                .set(FirestoreMapper.entryToMap(entry), SetOptions.merge())
                .await()
            entryDao.updateSyncStatus(entry.id, SyncStatus.SYNCED)
        } catch (e: Exception) {
            Log.w(TAG, "pushEntry failed for ${entry.id}: ${e.message}")
            // Leave as PENDING_UPLOAD — WorkManager will retry
        }
    }

    /** Mark an entry as deleted in Firestore (tombstone). */
    suspend fun pushEntryDeletion(entryId: String) {
        val col = userCollection(ENTRIES) ?: return
        try {
            col.document(entryId)
                .update("_deleted", true, "updatedAt", System.currentTimeMillis())
                .await()
            entryDao.hardDelete(entryId)
        } catch (e: Exception) {
            Log.w(TAG, "pushEntryDeletion failed for $entryId: ${e.message}")
            // Leave as PENDING_DELETE — WorkManager will retry
        }
    }

    /** Push a single goal to Firestore. */
    suspend fun pushGoal(goal: GoalEntity) {
        val col = userCollection(GOALS) ?: return
        try {
            col.document(goal.id)
                .set(FirestoreMapper.goalToMap(goal), SetOptions.merge())
                .await()
            goalDao.updateSyncStatus(goal.id, SyncStatus.SYNCED)
        } catch (e: Exception) {
            Log.w(TAG, "pushGoal failed for ${goal.id}: ${e.message}")
        }
    }

    /** Mark a goal as deleted in Firestore. */
    suspend fun pushGoalDeletion(goalId: String) {
        val col = userCollection(GOALS) ?: return
        try {
            col.document(goalId)
                .update("_deleted", true, "updatedAt", System.currentTimeMillis())
                .await()
            goalDao.hardDelete(goalId)
        } catch (e: Exception) {
            Log.w(TAG, "pushGoalDeletion failed for $goalId: ${e.message}")
        }
    }

    /** Push a single check-in to Firestore. */
    suspend fun pushCheckIn(checkIn: GoalCheckInEntity) {
        val col = userCollection(GOAL_CHECKINS) ?: return
        try {
            col.document(checkIn.id)
                .set(FirestoreMapper.checkInToMap(checkIn), SetOptions.merge())
                .await()
            goalCheckInDao.updateSyncStatus(checkIn.id, SyncStatus.SYNCED)
        } catch (e: Exception) {
            Log.w(TAG, "pushCheckIn failed for ${checkIn.id}: ${e.message}")
        }
    }

    /** Push a single reminder to Firestore. */
    suspend fun pushReminder(reminder: WritingReminderEntity) {
        val col = userCollection(REMINDERS) ?: return
        try {
            col.document(reminder.id)
                .set(FirestoreMapper.reminderToMap(reminder), SetOptions.merge())
                .await()
            reminderDao.updateSyncStatus(reminder.id, SyncStatus.SYNCED)
        } catch (e: Exception) {
            Log.w(TAG, "pushReminder failed for ${reminder.id}: ${e.message}")
        }
    }

    /** Mark a reminder as deleted in Firestore. */
    suspend fun pushReminderDeletion(reminderId: String) {
        val col = userCollection(REMINDERS) ?: return
        try {
            col.document(reminderId)
                .update("_deleted", true, "updatedAt", System.currentTimeMillis())
                .await()
            reminderDao.hardDelete(reminderId)
        } catch (e: Exception) {
            Log.w(TAG, "pushReminderDeletion failed for $reminderId: ${e.message}")
        }
    }

    /** Push a single preference to Firestore (only for allowlisted keys). */
    suspend fun pushPreference(key: String, value: String) {
        if (key !in SYNCABLE_PREF_KEYS) return
        val col = userCollection(PREFERENCES) ?: return
        try {
            col.document(key)
                .set(FirestoreMapper.preferenceToMap(value), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "pushPreference failed for $key: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════════
    // PUSH ALL: Batch push pending changes
    // Called by SyncWorker on periodic schedule
    // ═══════════════════════════════════════════════

    suspend fun pushPendingChanges() {
        if (uid == null) return
        _syncState.value = SyncState.Syncing

        try {
            var pushedCount = 0

            // Push pending entries
            val pendingEntries = entryDao.getPendingSyncEntries()
            for (entry in pendingEntries) {
                if (entry.syncStatus == SyncStatus.PENDING_DELETE) {
                    pushEntryDeletion(entry.id)
                } else {
                    pushEntry(entry)
                }
                pushedCount++
            }

            // Push pending goals
            val pendingGoals = goalDao.getPendingSyncGoals()
            for (goal in pendingGoals) {
                if (goal.syncStatus == SyncStatus.PENDING_DELETE) {
                    pushGoalDeletion(goal.id)
                } else {
                    pushGoal(goal)
                }
                pushedCount++
            }

            // Push pending check-ins
            val pendingCheckIns = goalCheckInDao.getPendingSyncCheckIns()
            for (checkIn in pendingCheckIns) {
                if (checkIn.syncStatus == SyncStatus.PENDING_DELETE) {
                    goalCheckInDao.hardDelete(checkIn.id)
                } else {
                    pushCheckIn(checkIn)
                }
                pushedCount++
            }

            // Push pending reminders
            val pendingReminders = reminderDao.getPendingSyncReminders()
            for (reminder in pendingReminders) {
                if (reminder.syncStatus == SyncStatus.PENDING_DELETE) {
                    pushReminderDeletion(reminder.id)
                } else {
                    pushReminder(reminder)
                }
                pushedCount++
            }

            // Push syncable preferences
            val syncablePrefs = preferenceDao.getBatch(SYNCABLE_PREF_KEYS.toList())
            for (pref in syncablePrefs) {
                pushPreference(pref.key, pref.value)
            }

            // Update sync metadata
            userCollection(SYNC_META)?.document("status")?.set(
                mapOf(
                    "lastSyncedAt" to System.currentTimeMillis(),
                    "appVersion" to "2.0"
                ),
                SetOptions.merge()
            )?.await()

            if (pushedCount > 0) {
                analyticsService.logSyncPushSuccess("batch", pushedCount)
            }

            _syncState.value = SyncState.Idle
        } catch (e: Exception) {
            Log.e(TAG, "pushPendingChanges failed: ${e.message}")
            analyticsService.logSyncPushFailed("batch", e.message ?: "unknown")
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
        }
    }

    // ═══════════════════════════════════════════════
    // CLOUD DATA CHECK: Does the user have cloud data?
    // Used by RestoreService to decide what to do on sign-in
    // ═══════════════════════════════════════════════

    /** Returns the number of non-deleted entries in Firestore for the current user. */
    suspend fun getCloudEntryCount(): Int {
        val col = userCollection(ENTRIES) ?: return 0
        return try {
            val snap = col.whereNotEqualTo("_deleted", true).get().await()
            snap.size()
        } catch (e: Exception) {
            Log.w(TAG, "getCloudEntryCount failed: ${e.message}")
            0
        }
    }

    /** Returns the number of non-deleted goals in Firestore for the current user. */
    suspend fun getCloudGoalCount(): Int {
        val col = userCollection(GOALS) ?: return 0
        return try {
            val snap = col.whereNotEqualTo("_deleted", true).get().await()
            snap.size()
        } catch (e: Exception) {
            0
        }
    }
}
