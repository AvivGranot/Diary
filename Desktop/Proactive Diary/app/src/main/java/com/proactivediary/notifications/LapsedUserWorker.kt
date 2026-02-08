package com.proactivediary.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.proactivediary.MainActivity
import com.proactivediary.R
import com.proactivediary.data.repository.EntryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class LapsedUserWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val entryRepository: EntryRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "lapsed_user_check"
        private const val NOTIFICATION_ID = 9001
        private const val INACTIVITY_DAYS = 3L
    }

    override suspend fun doWork(): Result {
        val lastEntryMs = entryRepository.getLastEntryTimestamp() ?: return Result.success()

        val lastEntryDate = Instant.ofEpochMilli(lastEntryMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val daysSinceLastEntry = LocalDate.now().toEpochDay() - lastEntryDate.toEpochDay()

        if (daysSinceLastEntry >= INACTIVITY_DAYS) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_LAPSED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Proactive Diary")
            .setContentText("Your practice is here when you\u2019re ready.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted — silently skip
        }
    }
}
