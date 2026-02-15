package com.proactivediary.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class NotificationHealth {
    data object Healthy : NotificationHealth()
    data class Degraded(val reasons: List<HealthIssue>) : NotificationHealth()
    data class Blocked(val reasons: List<HealthIssue>) : NotificationHealth()
}

enum class HealthIssue {
    NOTIFICATIONS_DISABLED,
    WRITING_CHANNEL_DISABLED,
    GOALS_CHANNEL_DISABLED,
    EXACT_ALARMS_DENIED,
    BATTERY_OPTIMIZED
}

@Singleton
class NotificationHealthChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun areNotificationsEnabled(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun isChannelEnabled(channelId: String): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = nm.getNotificationChannel(channelId) ?: return false
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun isBatteryOptimized(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return !pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun getOverallHealth(): NotificationHealth {
        val issues = mutableListOf<HealthIssue>()

        if (!areNotificationsEnabled()) {
            issues.add(HealthIssue.NOTIFICATIONS_DISABLED)
        }
        if (!isChannelEnabled(NotificationChannels.CHANNEL_WRITING)) {
            issues.add(HealthIssue.WRITING_CHANNEL_DISABLED)
        }
        if (!isChannelEnabled(NotificationChannels.CHANNEL_GOALS)) {
            issues.add(HealthIssue.GOALS_CHANNEL_DISABLED)
        }
        if (!canScheduleExactAlarms()) {
            issues.add(HealthIssue.EXACT_ALARMS_DENIED)
        }
        if (isBatteryOptimized()) {
            issues.add(HealthIssue.BATTERY_OPTIMIZED)
        }

        return when {
            issues.isEmpty() -> NotificationHealth.Healthy
            HealthIssue.NOTIFICATIONS_DISABLED in issues -> NotificationHealth.Blocked(issues)
            else -> NotificationHealth.Degraded(issues)
        }
    }

    fun getNotificationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    }

    fun getBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun getExactAlarmSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            getNotificationSettingsIntent()
        }
    }
}
