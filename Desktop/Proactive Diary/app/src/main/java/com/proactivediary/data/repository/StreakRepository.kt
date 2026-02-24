package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.PreferenceDao
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val entryDao: EntryDao,
    private val preferenceDao: PreferenceDao
) {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    /**
     * Backfill-aware day range: uses COALESCE(entry_date, created_at).
     */
    private suspend fun hasDayEntryWithBackfill(date: LocalDate): Boolean {
        val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return entryDao.countEntriesForDayWithBackfill(startOfDay, endOfDay) > 0
    }

    /**
     * Compassionate streak: walks backward from today, allows configurable grace gaps.
     * Grace days: 1-2 days can be skipped without breaking the streak.
     */
    suspend fun calculateWritingStreak(): Int {
        val graceDays = try {
            preferenceDao.getValue("streak_grace_days")?.toIntOrNull() ?: 1
        } catch (_: Exception) { 1 }

        val today = LocalDate.now()
        var streak = 0
        var checkDate = today
        var gapDays = 0

        while (true) {
            if (hasDayEntryWithBackfill(checkDate)) {
                streak++
                gapDays = 0
                checkDate = checkDate.minusDays(1)
            } else if (checkDate == today) {
                // Today has no entry yet — don't count it as a gap
                checkDate = checkDate.minusDays(1)
            } else {
                gapDays++
                if (gapDays > graceDays) break
                checkDate = checkDate.minusDays(1)
            }
        }
        return streak
    }

    suspend fun hasWrittenToday(): Boolean {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return entryDao.countEntriesForDay(startOfDay, endOfDay) > 0
    }

    /**
     * Compassionate message: never shows "0 day streak". Instead, provides
     * encouraging context about the user's writing pattern.
     */
    suspend fun getCompassionateMessage(weekTarget: Int = 4): String {
        val streak = calculateWritingStreak()
        val daysThisWeek = getDaysWrittenThisWeek()

        return when {
            streak == 0 && daysThisWeek == 0 -> "Welcome back. Pick up where you left off."
            streak == 0 && daysThisWeek > 0 -> "You wrote $daysThisWeek day${if (daysThisWeek > 1) "s" else ""} this week. That counts."
            streak == 1 -> "Your first entry. Every journey starts here."
            streak < 7 -> "$streak days in a row. Keep going."
            else -> "$streak day streak. You\u2019re doing this."
        }
    }

    /**
     * Weekly consistency label: "3/4 days this week"
     */
    suspend fun getWeeklyConsistencyLabel(weekTarget: Int = 4): String {
        val daysWritten = getDaysWrittenThisWeek()
        return "$daysWritten/$weekTarget days this week"
    }

    /**
     * Streak freeze: 2 free freezes per month, auto-consumed when streak would break.
     * Returns true if a freeze was consumed.
     */
    suspend fun tryConsumeFreeze(): Boolean {
        val currentMonth = LocalDate.now().monthValue.toString() + "_" + LocalDate.now().year
        val key = "streak_freezes_$currentMonth"
        val usedFreezes = try {
            preferenceDao.getValue(key)?.toIntOrNull() ?: 0
        } catch (_: Exception) { 0 }

        return if (usedFreezes < 2) {
            try {
                preferenceDao.insert(
                    com.proactivediary.data.db.entities.PreferenceEntity(key, (usedFreezes + 1).toString())
                )
            } catch (_: Exception) { }
            true
        } else {
            false
        }
    }

    /**
     * Compassionate streak: counts how many of the last N weeks
     * the user hit their weekly writing target (default: 4 days/week).
     */
    suspend fun calculateWeeklyConsistency(
        weekTarget: Int = 4,
        weeksToCheck: Int = 12
    ): Pair<Int, Int> {
        val today = LocalDate.now()
        var weeksOnTarget = 0
        var weeksChecked = 0

        for (w in 0 until weeksToCheck) {
            val weekStart = today.minusWeeks(w.toLong()).with(DayOfWeek.MONDAY)
            val weekEnd = weekStart.plusDays(6)
            val effectiveEnd = if (weekEnd.isAfter(today)) today else weekEnd

            var daysWithEntries = 0
            var d = weekStart
            while (!d.isAfter(effectiveEnd)) {
                if (hasDayEntryWithBackfill(d)) {
                    daysWithEntries++
                }
                d = d.plusDays(1)
            }

            weeksChecked++
            if (daysWithEntries >= weekTarget) {
                weeksOnTarget++
            }
        }

        return Pair(weeksOnTarget, weeksChecked)
    }

    suspend fun getConsistencyPercentage(weekTarget: Int = 4, weeksToCheck: Int = 12): Int {
        val (onTarget, total) = calculateWeeklyConsistency(weekTarget, weeksToCheck)
        if (total == 0) return 0
        return ((onTarget.toFloat() / total) * 100).toInt()
    }

    /**
     * How many days this week the user has written (backfill-aware).
     */
    suspend fun getDaysWrittenThisWeek(): Int {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        var count = 0
        var d = monday
        while (!d.isAfter(today)) {
            if (hasDayEntryWithBackfill(d)) {
                count++
            }
            d = d.plusDays(1)
        }
        return count
    }

    /**
     * Weekly milestone check: at 4, 8, 12, 26, 52 weeks on target.
     */
    suspend fun isWeeklyMilestone(): Int {
        val (weeksOnTarget, _) = calculateWeeklyConsistency()
        return if (weeksOnTarget in listOf(4, 8, 12, 26, 52)) weeksOnTarget else 0
    }
}
