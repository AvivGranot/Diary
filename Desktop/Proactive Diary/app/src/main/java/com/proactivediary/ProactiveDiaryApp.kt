package com.proactivediary

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.proactivediary.notifications.LapsedUserWorker
import com.proactivediary.notifications.NotificationChannels
import com.proactivediary.notifications.NotificationService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ProactiveDiaryApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationService: NotificationService

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createChannels(this)
        scheduleLapsedUserCheck()
        rescheduleAlarms()
        initializeCrashlytics()
    }

    private fun rescheduleAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ProactiveDiaryApp", "rescheduleAlarms: starting rescheduleAll()")
                notificationService.rescheduleAll()
                Log.d("ProactiveDiaryApp", "rescheduleAlarms: completed successfully")
            } catch (e: Exception) {
                Log.e("ProactiveDiaryApp", "rescheduleAlarms: failed", e)
            }
        }
    }

    private fun scheduleLapsedUserCheck() {
        val request = PeriodicWorkRequestBuilder<LapsedUserWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LapsedUserWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun initializeCrashlytics() {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("plan_type", "unknown")
            setCustomKey("total_entries", 0)
            setCustomKey("days_since_install", 0)
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
        }
    }
}
