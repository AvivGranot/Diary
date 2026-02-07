package com.proactivediary.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val CHANNEL_WRITING = "writing_reminders"
    const val CHANNEL_GOALS = "goal_reminders"

    fun createChannels(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val writingChannel = NotificationChannel(
            CHANNEL_WRITING,
            "Writing Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to write in your diary"
        }

        val goalsChannel = NotificationChannel(
            CHANNEL_GOALS,
            "Goal Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for your goal check-ins"
        }

        notificationManager.createNotificationChannel(writingChannel)
        notificationManager.createNotificationChannel(goalsChannel)
    }
}
