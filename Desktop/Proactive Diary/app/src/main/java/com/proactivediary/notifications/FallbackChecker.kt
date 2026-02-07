package com.proactivediary.notifications

import android.content.Context
import com.proactivediary.data.db.dao.EntryDao
import dagger.hilt.android.EntryPointAccessors
import java.util.Calendar
import java.util.TimeZone

/**
 * Checks whether the user has written a diary entry today.
 * Used by the fallback notification mechanism to decide
 * whether to send a nudge notification.
 */
object FallbackChecker {

    /**
     * Returns true if the user has written at least one entry today.
     * Uses the Hilt-managed EntryDao singleton instead of creating a new DB instance.
     */
    suspend fun hasWrittenToday(context: Context): Boolean {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                FallbackCheckerEntryPoint::class.java
            )
            val entryDao = entryPoint.entryDao()
            val (startOfDay, endOfDay) = getTodayRange()
            val count = entryDao.countEntriesForDay(startOfDay, endOfDay)
            count > 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns a Pair of (startOfDayMillis, endOfDayMillis) for today.
     */
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis - 1
        return Pair(startOfDay, endOfDay)
    }
}
