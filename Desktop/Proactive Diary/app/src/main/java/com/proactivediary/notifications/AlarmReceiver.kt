package com.proactivediary.notifications

import android.Manifest
import android.app.AlarmManager
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
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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

        // Test notifications don't reschedule and ignore day checks
        if (type == TYPE_TEST) {
            showTestNotification(context)
            return
        }

        // Always reschedule tomorrow's exact alarm (since we no longer use setRepeating)
        rescheduleNextAlarm(context, intent, type, id)

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

        // Get beautiful content from the engine
        val content = NotificationContentEngine.getWritingContent()
        val notificationId = reminderId.hashCode() and 0x7FFFFFFF

        // Main tap intent — carries the prompt through the deep link chain
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
            putExtra(EXTRA_PROMPT, content.prompt)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Start Writing" action — same deep link with prompt
        val startWritingIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
            putExtra(EXTRA_PROMPT, content.prompt)
        }
        val startWritingPending = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 500000,
            startWritingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Remind Me Later" action — snooze 30 min
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(SnoozeReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TYPE, TYPE_WRITING)
            putExtra(EXTRA_ID, reminderId)
            putExtra(EXTRA_LABEL, label)
            putExtra(EXTRA_DAYS, "[0,1,2,3,4,5,6]")
            putExtra(EXTRA_TIME, "")
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 600000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_WRITING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(content.body + "\n\n\u201C" + content.prompt + "\u201D"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Start Writing", startWritingPending)
            .addAction(0, "Remind Me Later", snoozePending)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showGoalNotification(context: Context, goalId: String, goalTitle: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val content = NotificationContentEngine.getGoalContent(goalTitle)
        val notificationId = (goalId.hashCode() and 0x7FFFFFFF) or 0x40000000

        // Main tap intent — opens Goals screen
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_GOALS)
            putExtra(EXTRA_GOAL_ID, goalId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            goalId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Check In" action — records check-in without opening app
        val checkInIntent = Intent(context, GoalCheckInReceiver::class.java).apply {
            putExtra(GoalCheckInReceiver.EXTRA_GOAL_ID, goalId)
            putExtra(GoalCheckInReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val checkInPending = PendingIntent.getBroadcast(
            context,
            goalId.hashCode() + 700000,
            checkInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Remind Me Later" action
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(SnoozeReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TYPE, TYPE_GOAL)
            putExtra(EXTRA_ID, goalId)
            putExtra(EXTRA_LABEL, goalTitle)
            putExtra(EXTRA_DAYS, "[0,1,2,3,4,5,6]")
            putExtra(EXTRA_TIME, "")
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            goalId.hashCode() + 800000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_GOALS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Check In", checkInPending)
            .addAction(0, "Remind Me Later", snoozePending)
            .build()

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

        val content = NotificationContentEngine.getFallbackContent(streak)
        val notificationId = (reminderId.hashCode() and 0x7FFFFFFF) or 0x20000000

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
            putExtra(EXTRA_PROMPT, content.prompt)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 200000,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_WRITING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showTestNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TAB, TAB_WRITE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            TEST_NOTIFICATION_ID,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_WRITING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notifications are working.")
            .setContentText("You'll hear from us at your next scheduled reminder.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(TEST_NOTIFICATION_ID, notification)
    }

    /**
     * Reschedule the next day's exact alarm. Since we use setExactAndAllowWhileIdle
     * (one-shot) instead of setRepeating, each alarm must schedule the next one.
     */
    private fun rescheduleNextAlarm(context: Context, intent: Intent, type: String, id: String) {
        val time = intent.getStringExtra(EXTRA_TIME) ?: return
        val parts = time.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val requestCode = when (type) {
            TYPE_WRITING -> (id.hashCode() and 0x7FFFFFFF) % 100000
            TYPE_GOAL -> ((id.hashCode() and 0x7FFFFFFF) % 100000) + 100000
            TYPE_FALLBACK -> ((id.hashCode() and 0x7FFFFFFF) % 100000) + 200000
            else -> return
        }

        // Clone the original intent with all extras for the next firing
        val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtras(intent)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, tomorrow.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, tomorrow.timeInMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, tomorrow.timeInMillis, pendingIntent
            )
        }
    }

    companion object {
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_ID = "notification_id"
        const val EXTRA_LABEL = "notification_label"
        const val EXTRA_DAYS = "notification_days"
        const val EXTRA_TIME = "notification_time"
        const val EXTRA_FALLBACK_ENABLED = "fallback_enabled"
        const val EXTRA_TAB = "destination"
        const val EXTRA_PROMPT = "notification_prompt"
        const val EXTRA_GOAL_ID = "notification_goal_id"

        const val TYPE_WRITING = "writing"
        const val TYPE_GOAL = "goal"
        const val TYPE_FALLBACK = "fallback_check"
        const val TYPE_TEST = "test"

        const val TAB_WRITE = "write"
        const val TAB_GOALS = "goals"

        const val TEST_NOTIFICATION_ID = 9998
    }
}
