package com.proactivediary.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalDao
import com.proactivediary.data.db.dao.WritingReminderDao
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: WritingReminderDao,
    private val goalDao: GoalDao,
    private val entryDao: EntryDao
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Always return true so reminders are never silently blocked.
     * scheduleExact() handles the API 31+ permission check internally.
     */
    private fun canScheduleAlarms(): Boolean = true

    private val gson = Gson()

    /**
     * Schedule an exact alarm for a writing reminder.
     * The alarm fires daily at the specified time; the receiver checks
     * if the current day of week is in the reminder's active days,
     * then reschedules the next day's alarm.
     */
    fun scheduleWritingReminder(reminder: WritingReminderEntity) {
        if (!reminder.isActive) return
        if (!canScheduleAlarms()) return

        val (hour, minute) = parseTime(reminder.time)
        val requestCode = generateWritingRequestCode(reminder.id)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_WRITING)
            putExtra(AlarmReceiver.EXTRA_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_LABEL, reminder.label)
            putExtra(AlarmReceiver.EXTRA_DAYS, reminder.days)
            putExtra(AlarmReceiver.EXTRA_TIME, reminder.time)
            putExtra(AlarmReceiver.EXTRA_FALLBACK_ENABLED, reminder.fallbackEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(hour, minute)
        scheduleExact(triggerTime, pendingIntent)
    }

    /**
     * Schedule an exact alarm for a goal reminder.
     */
    fun scheduleGoalReminder(goal: GoalEntity) {
        if (!goal.isActive) return
        if (!canScheduleAlarms()) return

        val (hour, minute) = parseTime(goal.reminderTime)
        val requestCode = generateGoalRequestCode(goal.id)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_GOAL)
            putExtra(AlarmReceiver.EXTRA_ID, goal.id)
            putExtra(AlarmReceiver.EXTRA_LABEL, goal.title)
            putExtra(AlarmReceiver.EXTRA_DAYS, goal.reminderDays)
            putExtra(AlarmReceiver.EXTRA_TIME, goal.reminderTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(hour, minute)
        scheduleExact(triggerTime, pendingIntent)
    }

    /**
     * Schedule a one-shot fallback check 30 minutes after a writing reminder.
     * If the user hasn't written today, a nudge notification is shown.
     */
    fun scheduleFallbackCheck(reminder: WritingReminderEntity) {
        if (!reminder.fallbackEnabled) return
        if (!canScheduleAlarms()) return

        val (hour, minute) = parseTime(reminder.time)
        val requestCode = generateFallbackRequestCode(reminder.id)

        // 30 minutes after the reminder time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, 30)
        }

        // If the fallback time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_FALLBACK)
            putExtra(AlarmReceiver.EXTRA_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_LABEL, reminder.label)
            putExtra(AlarmReceiver.EXTRA_DAYS, reminder.days)
            putExtra(AlarmReceiver.EXTRA_TIME, reminder.time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(calendar.timeInMillis, pendingIntent)
    }

    /**
     * Cancel a writing reminder alarm and its fallback.
     */
    fun cancelReminder(reminderId: String) {
        cancelAlarm(generateWritingRequestCode(reminderId))
        cancelAlarm(generateFallbackRequestCode(reminderId))
    }

    /**
     * Cancel a goal reminder alarm.
     */
    fun cancelGoalReminder(goalId: String) {
        cancelAlarm(generateGoalRequestCode(goalId))
    }

    /**
     * Re-schedule all active reminders and goals.
     * Called after device boot or when the app needs to restore alarms.
     */
    suspend fun rescheduleAll() {
        // Reschedule all active writing reminders
        val reminders = reminderDao.getActiveRemindersSync()
        Log.d("NotificationService", "rescheduleAll: found ${reminders.size} active reminders")
        for (reminder in reminders) {
            Log.d("NotificationService", "  scheduling reminder: id=${reminder.id}, time=${reminder.time}, active=${reminder.isActive}")
            scheduleWritingReminder(reminder)
            if (reminder.fallbackEnabled) {
                scheduleFallbackCheck(reminder)
            }
        }

        // Reschedule all active goal reminders
        val goals = goalDao.getActiveGoalsSync()
        Log.d("NotificationService", "rescheduleAll: found ${goals.size} active goals")
        for (goal in goals) {
            Log.d("NotificationService", "  scheduling goal: id=${goal.id}, title=${goal.title}, time=${goal.reminderTime}")
            scheduleGoalReminder(goal)
        }
    }

    /**
     * Schedule a test notification 5 seconds from now.
     * Proves the full pipeline: AlarmManager → AlarmReceiver → NotificationManager.
     */
    fun scheduleTestNotification() {
        val triggerTime = System.currentTimeMillis() + 5000

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_TEST)
            putExtra(AlarmReceiver.EXTRA_ID, "test")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmReceiver.TEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerTime, pendingIntent)
        Log.d("NotificationService", "Test notification scheduled for 5 seconds from now")
    }

    // --- Private helpers ---

    /**
     * Schedule an exact alarm that fires even in Doze mode.
     * Falls back to inexact-but-Doze-safe if exact alarm permission
     * is not granted on API 31+.
     */
    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            } else {
                Log.w("NotificationService", "Exact alarm permission not granted — using inexact fallback. Notifications may be delayed.")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        return try {
            val parts = time.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    /**
     * Calculate the next trigger time for a given hour and minute.
     * If the time has already passed today, schedule for tomorrow.
     */
    private fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    /**
     * Generate a stable request code from a string ID.
     * Uses different offset ranges to avoid collisions between types.
     */
    private fun generateWritingRequestCode(id: String): Int =
        (id.hashCode() and 0x7FFFFFFF) % 100000

    private fun generateGoalRequestCode(id: String): Int =
        ((id.hashCode() and 0x7FFFFFFF) % 100000) + 100000

    private fun generateFallbackRequestCode(id: String): Int =
        ((id.hashCode() and 0x7FFFFFFF) % 100000) + 200000

    companion object {
        /**
         * Parse the JSON days string "[0,1,2,3,4,5,6]" into a list of ints.
         * Day indices: 0=Mon, 1=Tue, 2=Wed, 3=Thu, 4=Fri, 5=Sat, 6=Sun
         */
        fun parseDays(daysJson: String): List<Int> {
            return try {
                val type = object : TypeToken<List<Int>>() {}.type
                Gson().fromJson<List<Int>>(daysJson, type) ?: (0..6).toList()
            } catch (e: Exception) {
                (0..6).toList()
            }
        }

        /**
         * Check if today's day-of-week (Mon=0..Sun=6) is in the given days list.
         */
        fun isTodayInDays(daysJson: String): Boolean {
            val days = parseDays(daysJson)
            val todayCalendar = Calendar.getInstance()
            // Calendar: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
            // Our format: MON=0, TUE=1, ..., SUN=6
            val calendarDay = todayCalendar.get(Calendar.DAY_OF_WEEK)
            val ourDay = when (calendarDay) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            return ourDay in days
        }
    }
}
