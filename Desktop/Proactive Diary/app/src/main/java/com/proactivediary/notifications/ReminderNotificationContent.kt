package com.proactivediary.notifications

import androidx.core.app.NotificationCompat

/**
 * Pure data class holding notification content derived from the
 * Reminder Tone experiment variant. Testable without Android context.
 */
data class ReminderNotificationContent(
    val title: String?,
    val body: String?,
    val isSilent: Boolean,
    val priority: Int
)

/**
 * Map a Reminder Tone experiment variant + label to notification content.
 * Pure function — no Android dependencies, trivially unit-testable.
 */
fun buildReminderContent(toneVariant: String, label: String): ReminderNotificationContent {
    return when (toneVariant) {
        "silent" -> ReminderNotificationContent(
            title = null,
            body = null,
            isSilent = true,
            priority = NotificationCompat.PRIORITY_LOW
        )
        "gentle" -> ReminderNotificationContent(
            title = "Your diary is here when you're ready",
            body = label,
            isSilent = false,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
        else -> ReminderNotificationContent(
            title = "Time to write",
            body = label,
            isSilent = false,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
}
