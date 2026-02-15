package com.proactivediary.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.proactivediary.R
import com.proactivediary.data.db.dao.GoalCheckInDao
import com.proactivediary.data.db.entities.GoalCheckInEntity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/**
 * Handles the "Check In" notification action for goals.
 * Records a check-in without opening the app, then shows a brief confirmation.
 */
class GoalCheckInReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GoalCheckInEntryPoint {
        fun goalCheckInDao(): GoalCheckInDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getStringExtra(EXTRA_GOAL_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        // Dismiss the original notification
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationId != 0) {
            nm.cancel(notificationId)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    GoalCheckInEntryPoint::class.java
                )
                val checkInDao = entryPoint.goalCheckInDao()

                val today = LocalDate.now().toString()
                val existing = checkInDao.getCheckInForDate(goalId, today)
                if (existing == null) {
                    checkInDao.insert(
                        GoalCheckInEntity(
                            id = UUID.randomUUID().toString(),
                            goalId = goalId,
                            date = today,
                            completed = true,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                // Show brief confirmation notification
                val confirmation = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_GOALS)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Checked in.")
                    .setContentText("Keep going. Every day counts.")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
                    .build()

                nm.notify(CONFIRMATION_NOTIFICATION_ID, confirmation)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_GOAL_ID = "goal_check_in_goal_id"
        const val EXTRA_NOTIFICATION_ID = "goal_check_in_notification_id"
        private const val CONFIRMATION_NOTIFICATION_ID = 9999
    }
}
