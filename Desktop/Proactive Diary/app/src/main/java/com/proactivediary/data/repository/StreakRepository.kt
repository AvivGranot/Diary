package com.proactivediary.data.repository

import com.proactivediary.data.db.dao.EntryDao
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val entryDao: EntryDao
) {
    suspend fun calculateWritingStreak(): Int {
        val today = LocalDate.now()
        var streak = 0
        var checkDate = today

        while (true) {
            val startOfDay = checkDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = checkDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            val count = entryDao.countEntriesForDay(startOfDay, endOfDay)

            if (count > 0) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (checkDate == today) {
                // Today has no entry yet — check if yesterday had one (streak is still alive)
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun hasWrittenToday(): Boolean {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return entryDao.countEntriesForDay(startOfDay, endOfDay) > 0
    }
}
