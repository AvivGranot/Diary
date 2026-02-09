package com.proactivediary.ui.goals

import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.domain.model.GoalFrequency
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class GoalsViewModelTest {

    // ─── calculateStreak tests ───

    @Test
    fun `calculateStreak returns 0 when no check-ins`() {
        val streak = GoalsViewModel.calculateStreak(emptyList())
        assertEquals(0, streak)
    }

    @Test
    fun `calculateStreak returns 1 when only today checked in`() {
        val today = LocalDate.now()
        val checkIns = listOf(checkIn("1", today))
        assertEquals(1, GoalsViewModel.calculateStreak(checkIns, today))
    }

    @Test
    fun `calculateStreak counts consecutive days`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn("1", today),
            checkIn("2", today.minusDays(1)),
            checkIn("3", today.minusDays(2)),
            checkIn("4", today.minusDays(3))
        )
        assertEquals(4, GoalsViewModel.calculateStreak(checkIns, today))
    }

    @Test
    fun `calculateStreak breaks on gap`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn("1", today),
            checkIn("2", today.minusDays(1)),
            // gap on day -2
            checkIn("3", today.minusDays(3))
        )
        assertEquals(2, GoalsViewModel.calculateStreak(checkIns, today))
    }

    @Test
    fun `calculateStreak allows yesterday as start when today not checked in`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn("1", today.minusDays(1)),
            checkIn("2", today.minusDays(2)),
            checkIn("3", today.minusDays(3))
        )
        assertEquals(3, GoalsViewModel.calculateStreak(checkIns, today))
    }

    @Test
    fun `calculateStreak returns 0 when last check-in is 2 days ago`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn("1", today.minusDays(2))
        )
        assertEquals(0, GoalsViewModel.calculateStreak(checkIns, today))
    }

    @Test
    fun `calculateStreak ignores incomplete check-ins`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            checkIn("1", today, completed = true),
            GoalCheckInEntity(
                id = "2", goalId = "g1",
                date = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                completed = false, createdAt = 0L
            )
        )
        assertEquals(1, GoalsViewModel.calculateStreak(checkIns, today))
    }

    // ─── calculateProgress tests ───

    @Test
    fun `calculateProgress daily returns 0 when no check-ins`() {
        val today = LocalDate.now()
        val goal = makeGoal("daily")
        val progress = GoalsViewModel.calculateProgress(goal, emptyList(), today)
        assertEquals(0, progress)
    }

    @Test
    fun `calculateProgress daily returns correct percentage`() {
        val today = LocalDate.of(2024, 1, 10)
        val goal = makeGoal("daily")
        // 5 check-ins out of 10 days elapsed
        val checkIns = (1..5).map { day ->
            checkIn("$day", LocalDate.of(2024, 1, day))
        }
        assertEquals(50, GoalsViewModel.calculateProgress(goal, checkIns, today))
    }

    @Test
    fun `calculateProgress daily caps at 100`() {
        val today = LocalDate.of(2024, 1, 5)
        val goal = makeGoal("daily")
        // 5 check-ins in 5 days
        val checkIns = (1..5).map { day ->
            checkIn("$day", LocalDate.of(2024, 1, day))
        }
        assertEquals(100, GoalsViewModel.calculateProgress(goal, checkIns, today))
    }

    @Test
    fun `calculateProgress weekly counts unique weeks`() {
        val today = LocalDate.of(2024, 1, 15)
        val goal = makeGoal("weekly")
        val checkIns = listOf(
            checkIn("1", LocalDate.of(2024, 1, 2)),  // week 1
            checkIn("2", LocalDate.of(2024, 1, 10))  // week 2
        )
        val progress = GoalsViewModel.calculateProgress(goal, checkIns, today)
        assertTrue(progress in 1..100)
    }

    @Test
    fun `calculateProgress monthly counts unique months`() {
        val today = LocalDate.of(2024, 3, 15)
        val goal = makeGoal("monthly")
        val checkIns = listOf(
            checkIn("1", LocalDate.of(2024, 1, 5)),
            checkIn("2", LocalDate.of(2024, 2, 10)),
            checkIn("3", LocalDate.of(2024, 3, 1))
        )
        assertEquals(100, GoalsViewModel.calculateProgress(goal, checkIns, today))
    }

    // ─── helpers ───

    private fun checkIn(id: String, date: LocalDate, completed: Boolean = true) =
        GoalCheckInEntity(
            id = id,
            goalId = "g1",
            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            completed = completed,
            createdAt = 0L
        )

    private fun makeGoal(frequency: String) = GoalEntity(
        id = "g1",
        title = "Test Goal",
        frequency = frequency,
        reminderTime = "09:00",
        reminderDays = "[0,1,2,3,4,5,6]",
        createdAt = 0L,
        updatedAt = 0L
    )
}
