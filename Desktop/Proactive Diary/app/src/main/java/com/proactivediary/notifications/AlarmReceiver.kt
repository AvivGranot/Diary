package com.proactivediary.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.proactivediary.MainActivity
import com.proactivediary.R
import com.proactivediary.data.repository.StreakRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Check notification permission on API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val type = intent.getStringExtra(EXTRA_TYPE) ?: return
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val label = intent.getStringExtra(EXTRA_LABEL) ?: "Proactive Diary"
        val daysJson = intent.getStringExtra(EXTRA_DAYS) ?: "[0,1,2,3,4,5,6]"

        // Check if today is in the reminder's active days
        if (!NotificationService.isTodayInDays(daysJson)) return

        when (type) {
            TYPE_WRITING -> {
                showWritingNotification(context, id, label)
            }

            TYPE_GOAL -> {
                showGoalNotification(context, id, label)
            }

            TYPE_FALLBACK -> {
                handleFallbackCheck(context, id, label)
            }
        }
    }

    private fun showWritingNotification(context: Context, reminderId: String, label: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_WRITING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to write")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = reminderId.hashCode() and 0x7FFFFFFF
        notificationManager.notify(notificationId, notification)
    }

    private fun showGoalNotification(context: Context, goalId: String, goalTitle: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_GOALS)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            goalId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_GOALS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Goal check-in")
            .setContentText("Have you worked on \"$goalTitle\" today?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (goalId.hashCode() and 0x7FFFFFFF) or 0x40000000
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Fallback check: use goAsync() to perform an async DB check,
     * then show a nudge notification only if the user hasn't written today.
     * If the user has an active streak, personalize the message.
     */
    private fun handleFallbackCheck(context: Context, reminderId: String, label: String) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hasWritten = FallbackChecker.hasWrittenToday(context)
                if (!hasWritten) {
                    // Query streak to personalize the notification
                    val streak = try {
                        val entryPoint = EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            AlarmReceiverEntryPoint::class.java
                        )
                        val streakRepository = entryPoint.streakRepository()
                        streakRepository.calculateWritingStreak()
                    } catch (_: Exception) {
                        0
                    }
                    showFallbackNotification(context, reminderId, streak)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showFallbackNotification(context: Context, reminderId: String, streak: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 200000,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (streak > 0) {
            "You're on a $streak-day streak. Don't break it."
        } else {
            "You haven't written in your diary today. Even a few words count."
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_WRITING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Still time to write")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (reminderId.hashCode() and 0x7FFFFFFF) or 0x20000000
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_ID = "notification_id"
        const val EXTRA_LABEL = "notification_label"
        const val EXTRA_DAYS = "notification_days"
        const val EXTRA_FALLBACK_ENABLED = "fallback_enabled"
        const val EXTRA_TAB = "destination"

        const val TYPE_WRITING = "writing"
        const val TYPE_GOAL = "goal"
        const val TYPE_FALLBACK = "fallback_check"

        const val TAB_WRITE = "write"
        const val TAB_GOALS = "goals"
    }
}
