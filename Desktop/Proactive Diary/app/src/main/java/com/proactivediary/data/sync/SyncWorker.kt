package com.proactivediary.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.proactivediary.analytics.AnalyticsService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic background worker that pushes any pending local changes to Firestore.
 * Runs every 15 minutes (minimum WorkManager periodic interval) when network is available.
 * Also triggered as a one-shot after sign-in.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncService: SyncService,
    private val analyticsService: AnalyticsService
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "cloud_sync_periodic"
        const val WORK_NAME_ONESHOT = "cloud_sync_oneshot"
        private const val TAG = "SyncWorker"
        private const val MAX_RETRIES = 5
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "SyncWorker running (attempt $runAttemptCount)")
            syncService.pushPendingChanges()
            Log.d(TAG, "SyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "SyncWorker failed: ${e.message}")
            analyticsService.logSyncPushFailed("worker", e.message ?: "unknown")
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
