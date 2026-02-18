package com.proactivediary.data.sync

import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps PreferenceDao for design/theme preferences that should sync to Firestore.
 * Only allowlisted keys are pushed to cloud. All others are local-only.
 */
@Singleton
class SyncAwarePreferenceHelper @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val syncService: SyncService
) {
    /**
     * Insert a preference locally and sync to cloud if it's an allowlisted key.
     */
    suspend fun insert(pref: PreferenceEntity) {
        preferenceDao.insert(pref)
        if (pref.key in SyncService.SYNCABLE_PREF_KEYS) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    syncService.pushPreference(pref.key, pref.value)
                } catch (_: Exception) {
                    // Non-fatal: WorkManager will retry
                }
            }
        }
    }

    /**
     * Sync-aware insert (blocking variant for non-suspend contexts).
     */
    fun insertSync(pref: PreferenceEntity) {
        preferenceDao.insertSync(pref)
        if (pref.key in SyncService.SYNCABLE_PREF_KEYS) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    syncService.pushPreference(pref.key, pref.value)
                } catch (_: Exception) { }
            }
        }
    }
}
