package com.proactivediary.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val CHANNEL_WRITING = "writing_reminders_v2"
    const val CHANNEL_GOALS = "goal_reminders_v2"
    const val CHANNEL_LAPSED = "lapsed_user"

    fun createChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Delete old low-importance channels so upgraded ones take effect
        notificationManager.deleteNotificationChannel("writing_reminders")
        notificationManager.deleteNotificationChannel("goal_reminders")

        val writingChannel = NotificationChannel(
            CHANNEL_WRITING,
            "Writing Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to write in your diary"
        }

        val goalsChannel = NotificationChannel(
            CHANNEL_GOALS,
            "Goal Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for your goal check-ins"
        }

        val lapsedChannel = NotificationChannel(
            CHANNEL_LAPSED,
            "Gentle Reminders",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "A quiet nudge if you haven't written in a while"
        }

        notificationManager.createNotificationChannel(writingChannel)
        notificationManager.createNotificationChannel(goalsChannel)
        notificationManager.createNotificationChannel(lapsedChannel)
    }
}
