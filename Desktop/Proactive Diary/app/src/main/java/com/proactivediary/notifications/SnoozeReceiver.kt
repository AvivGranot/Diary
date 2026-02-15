package com.proactivediary.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Handles the "Remind Me Later" notification action.
 * Dismisses the current notification and reschedules 30 minutes from now.
 */
class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Dismiss the current notification
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        if (notificationId != 0) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notificationId)
        }

        // Reschedule 30 minutes from now
        val type = intent.getStringExtra(AlarmReceiver.EXTRA_TYPE) ?: return
        val id = intent.getStringExtra(AlarmReceiver.EXTRA_ID) ?: return

        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 30)
        }

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, type)
            putExtra(AlarmReceiver.EXTRA_ID, id)
            putExtra(AlarmReceiver.EXTRA_LABEL, intent.getStringExtra(AlarmReceiver.EXTRA_LABEL))
            putExtra(AlarmReceiver.EXTRA_DAYS, intent.getStringExtra(AlarmReceiver.EXTRA_DAYS))
            putExtra(AlarmReceiver.EXTRA_TIME, intent.getStringExtra(AlarmReceiver.EXTRA_TIME))
        }

        val requestCode = SNOOZE_REQUEST_CODE_BASE + (id.hashCode() and 0x7FFFFFFF) % 100000
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, snoozeTime.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, snoozeTime.timeInMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, snoozeTime.timeInMillis, pendingIntent
            )
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "snooze_notification_id"
        private const val SNOOZE_REQUEST_CODE_BASE = 300000
    }
}
