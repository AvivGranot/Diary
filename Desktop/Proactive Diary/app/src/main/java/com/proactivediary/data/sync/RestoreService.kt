package com.proactivediary.data.sync

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.AppDatabase
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.PreferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles data restoration on new device sign-in and guest-to-auth data merge.
 *
 * Decision matrix:
 * - No cloud data, no local data → Skip (fresh user)
 * - Cloud data, no local data → Full restore (new device)
 * - Cloud data + local data → Merge (LWW per document)
 * - No cloud data, local data → Push all local (guest upgrading to auth)
 */
@Singleton
class RestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val syncService: SyncService,
    private val imageSyncService: ImageSyncService,
    private val entryDao: EntryDao,
    private val goalDao: GoalDao,
    private val goalCheckInDao: GoalCheckInDao,
    private val reminderDao: WritingReminderDao,
    private val preferenceDao: PreferenceDao,
    private val analyticsService: AnalyticsService
) {
    companion object {
        private const val TAG = "RestoreService"
    }

    sealed class RestoreResult {
        object NoCloudData : RestoreResult()
        object Skipped : RestoreResult()
        data class Restored(val entries: Int, val goals: Int) : RestoreResult()
        data class PushedLocal(val entries: Int) : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }

    /**
     * Main entry point: called after successful sign-in.
     * Determines whether to push local data, pull cloud data, or merge.
     */
    suspend fun checkAndRestore(): RestoreResult {
        val uid = auth.currentUser?.uid ?: return RestoreResult.Skipped
        val startTime = System.currentTimeMillis()

        return try {
            val cloudEntryCount = syncService.getCloudEntryCount()
            val localEntryCount = entryDao.getTotalCount()

            Log.d(TAG, "checkAndRestore: cloud=$cloudEntryCount, local=$localEntryCount")

            when {
                cloudEntryCount == 0 && localEntryCount == 0 -> {
                    // Fresh user — nothing to do
                    RestoreResult.NoCloudData
                }
                cloudEntryCount > 0 && localEntryCount == 0 -> {
                    // New device — full restore
                    analyticsService.logSyncRestoreStarted(cloudEntryCount)
                    val result = fullRestore(uid)
                    val duration = System.currentTimeMillis() - startTime
                    analyticsService.logSyncRestoreCompleted(
                        result.first, result.second, duration
                    )
                    RestoreResult.Restored(result.first, result.second)
                }
                cloudEntryCount == 0 && localEntryCount > 0 -> {
                    // Guest upgrading to auth — push all local data
                    analyticsService.logGuestDataMerged(localEntryCount, 0)
                    syncService.pushPendingChanges()
                    RestoreResult.PushedLocal(localEntryCount)
                }
                else -> {
                    // Both have data — merge (push local, cloud data stays)
                    analyticsService.logGuestDataMerged(localEntryCount, cloudEntryCount)
                    syncService.pushPendingChanges()
                    RestoreResult.PushedLocal(localEntryCount)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAndRestore failed: ${e.message}")
            analyticsService.logSyncRestoreFailed(e.message ?: "unknown")
            RestoreResult.Error(e.message ?: "Restore failed")
        }
    }

    /**
     * Full restore: pull all cloud data into Room.
     * Returns (entriesRestored, goalsRestored).
     */
    private suspend fun fullRestore(uid: String): Pair<Int, Int> {
        val userDoc = firestore.collection("users").document(uid)
        var entriesRestored = 0
        var goalsRestored = 0

        // 1. Restore entries
        val entriesSnap = userDoc.collection(SyncService.ENTRIES)
            .whereNotEqualTo("_deleted", true)
            .get().await()

        for (doc in entriesSnap.documents) {
            val entry = FirestoreMapper.documentToEntry(doc) ?: continue
            entryDao.insert(entry)
            entriesRestored++
        }

        // Rebuild FTS index after bulk insert
        if (entriesRestored > 0) {
            try {
                entryDao.rawExec(SimpleSQLiteQuery("INSERT INTO entries_fts(entries_fts) VALUES('rebuild')"))
            } catch (e: Exception) {
                Log.w(TAG, "FTS rebuild failed: ${e.message}")
            }
        }

        // 2. Restore goals
        val goalsSnap = userDoc.collection(SyncService.GOALS)
            .whereNotEqualTo("_deleted", true)
            .get().await()

        for (doc in goalsSnap.documents) {
            val goal = FirestoreMapper.documentToGoal(doc) ?: continue
            goalDao.insert(goal)
            goalsRestored++
        }

        // 3. Restore goal check-ins
        val checkInsSnap = userDoc.collection(SyncService.GOAL_CHECKINS)
            .whereNotEqualTo("_deleted", true)
            .get().await()

        for (doc in checkInsSnap.documents) {
            val checkIn = FirestoreMapper.documentToCheckIn(doc) ?: continue
            try {
                goalCheckInDao.insert(checkIn)
            } catch (_: Exception) {
                // Ignore duplicate constraint violations
            }
        }

        // 4. Restore reminders
        val remindersSnap = userDoc.collection(SyncService.REMINDERS)
            .whereNotEqualTo("_deleted", true)
            .get().await()

        for (doc in remindersSnap.documents) {
            val reminder = FirestoreMapper.documentToReminder(doc) ?: continue
            reminderDao.insert(reminder)
        }

        // 5. Restore syncable preferences
        val prefsSnap = userDoc.collection(SyncService.PREFERENCES)
            .get().await()

        for (doc in prefsSnap.documents) {
            val key = doc.id
            val value = doc.getString("value") ?: continue
            if (key in SyncService.SYNCABLE_PREF_KEYS) {
                preferenceDao.insert(PreferenceEntity(key, value))
            }
        }

        // 6. Download images in background (non-blocking)
        CoroutineScope(Dispatchers.IO).launch {
            for (doc in entriesSnap.documents) {
                val imagesJson = doc.getString("images") ?: "[]"
                val audioPath = doc.getString("audioPath")
                if (imagesJson != "[]") {
                    imageSyncService.downloadEntryImages(doc.id, imagesJson)
                }
                if (!audioPath.isNullOrBlank()) {
                    imageSyncService.downloadEntryAudio(doc.id, audioPath)
                }
            }
        }

        return Pair(entriesRestored, goalsRestored)
    }
}
