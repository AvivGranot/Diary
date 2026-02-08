package com.proactivediary.domain

import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.repository.GoalRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WritingGoalService @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * Check if any active goals are writing-related and auto-check-in.
     * A goal is considered writing-related if its title contains keywords like
     * "write", "journal", "diary", "entry", "writing".
     * Returns the list of goal IDs that were auto-checked-in.
     */
    suspend fun autoCheckInWritingGoals(): List<String> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val checkedInGoalIds = mutableListOf<String>()

        val goals = goalRepository.getActiveGoalsSync()
        for (goal in goals) {
            if (isWritingRelatedGoal(goal.title)) {
                val existing = goalRepository.getCheckInForDate(goal.id, today)
                if (existing == null) {
                    val checkIn = GoalCheckInEntity(
                        id = UUID.randomUUID().toString(),
                        goalId = goal.id,
                        date = today,
                        completed = true,
                        createdAt = System.currentTimeMillis()
                    )
                    goalRepository.insertCheckIn(checkIn)
                    checkedInGoalIds.add(goal.id)
                }
            }
        }

        return checkedInGoalIds
    }

    /**
     * Returns a progress string for the first writing-related goal,
     * e.g. "3/7 this week" for daily goals or "2 entries this month" for weekly goals.
     */
    suspend fun getWritingGoalProgress(): String? {
        val goals = goalRepository.getActiveGoalsSync()
        val writingGoal = goals.firstOrNull { isWritingRelatedGoal(it.title) } ?: return null

        val today = LocalDate.now()
        val checkIns = goalRepository.getCompletedCheckIns(writingGoal.id)

        return when (writingGoal.frequency) {
            "daily" -> {
                val thisWeekStart = today.with(DayOfWeek.MONDAY)
                val thisWeekCheckIns = checkIns.count { checkIn ->
                    val date = runCatching { LocalDate.parse(checkIn.date) }.getOrNull()
                    date != null && !date.isBefore(thisWeekStart) && !date.isAfter(today)
                }
                "$thisWeekCheckIns/7 this week"
            }
            "weekly" -> {
                val thisMonthStart = today.withDayOfMonth(1)
                val thisMonthCheckIns = checkIns.count { checkIn ->
                    val date = runCatching { LocalDate.parse(checkIn.date) }.getOrNull()
                    date != null && !date.isBefore(thisMonthStart) && !date.isAfter(today)
                }
                "$thisMonthCheckIns entries this month"
            }
            else -> null
        }
    }

    private fun isWritingRelatedGoal(title: String): Boolean {
        val keywords = listOf("write", "writing", "journal", "diary", "entry", "entries", "words")
        val lowerTitle = title.lowercase()
        return keywords.any { keyword -> lowerTitle.contains(keyword) }
    }
}
