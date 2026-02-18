package com.proactivediary

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.proactivediary.data.ai.AIInsightWorker
import com.proactivediary.notifications.LapsedUserWorker
import com.proactivediary.notifications.WeeklyDigestWorker
import com.proactivediary.notifications.NotificationChannels
import com.proactivediary.data.repository.TemplateRepository
import com.proactivediary.data.sync.SyncWorker
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

    @Inject
    lateinit var templateRepository: TemplateRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationChannels.createChannels(this)
        } catch (e: Exception) {
            Log.e("ProactiveDiaryApp", "Failed to create notification channels", e)
        }
        try {
            scheduleLapsedUserCheck()
            scheduleAIInsightGeneration()
            scheduleWeeklyDigest()
            scheduleCloudSync()
        } catch (e: Exception) {
            Log.e("ProactiveDiaryApp", "Failed to schedule workers", e)
        }
        rescheduleAlarms()
        try {
            initializeCrashlytics()
        } catch (e: Exception) {
            Log.e("ProactiveDiaryApp", "Failed to initialize Crashlytics", e)
        }
        initializeTemplates()
        initializePlaces()
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

    private fun scheduleAIInsightGeneration() {
        val request = PeriodicWorkRequestBuilder<AIInsightWorker>(
            7, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AIInsightWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleWeeklyDigest() {
        val request = PeriodicWorkRequestBuilder<WeeklyDigestWorker>(
            7, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeeklyDigestWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleCloudSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun initializeTemplates() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                templateRepository.ensureBuiltInTemplates()
            } catch (e: Exception) {
                Log.e("ProactiveDiaryApp", "initializeTemplates: failed", e)
            }
        }
    }

    private fun initializePlaces() {
        try {
            val apiKey = BuildConfig.PLACES_API_KEY
            if (apiKey.isNotBlank()) {
                Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
            }
        } catch (e: Exception) {
            Log.e("ProactiveDiaryApp", "Failed to initialize Places SDK", e)
        }
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
