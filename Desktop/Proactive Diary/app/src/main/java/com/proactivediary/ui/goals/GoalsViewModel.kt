package com.proactivediary.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.entities.GoalCheckInEntity
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.domain.model.GoalFrequency
import com.proactivediary.notifications.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

data class GoalUiState(
    val id: String,
    val title: String,
    val frequency: GoalFrequency,
    val reminderTime: String,
    val reminderDays: String,
    val progressPercent: Int,
    val streak: Int,
    val checkedInToday: Boolean
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _checkInTimestamps = MutableStateFlow(0L)

    val goals: StateFlow<List<GoalUiState>> =
        combine(
            goalRepository.getActiveGoals(),
            _checkInTimestamps
        ) { entities, _ ->
            entities.map { goal -> buildGoalUi(goal) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun checkIn(goalId: String) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existing = goalRepository.getCheckInForDate(goalId, today)
            if (existing != null) return@launch

            val checkIn = GoalCheckInEntity(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                date = today,
                completed = true,
                createdAt = System.currentTimeMillis()
            )
            goalRepository.insertCheckIn(checkIn)
            _checkInTimestamps.value = System.currentTimeMillis()
        }
    }

    fun addGoal(title: String, frequency: GoalFrequency, time: String, days: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val goal = GoalEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                frequency = frequency.name.lowercase(),
                reminderTime = time,
                reminderDays = days,
                createdAt = now,
                updatedAt = now
            )
            goalRepository.insertGoal(goal)
            notificationService.scheduleGoalReminder(goal)
        }
    }

    fun updateGoal(goalId: String, title: String, frequency: GoalFrequency, time: String, days: String) {
        viewModelScope.launch {
            val existing = goalRepository.getByIdSync(goalId) ?: return@launch
            val updated = existing.copy(
                title = title,
                frequency = frequency.name.lowercase(),
                reminderTime = time,
                reminderDays = days,
                updatedAt = System.currentTimeMillis()
            )
            goalRepository.updateGoal(updated)
            notificationService.cancelGoalReminder(goalId)
            notificationService.scheduleGoalReminder(updated)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            notificationService.cancelGoalReminder(goalId)
        }
    }

    private suspend fun buildGoalUi(goal: GoalEntity): GoalUiState {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val checkIns = goalRepository.getCompletedCheckIns(goal.id)
        val todayCheckIn = goalRepository.getCheckInForDate(goal.id, todayStr)

        return GoalUiState(
            id = goal.id,
            title = goal.title,
            frequency = GoalFrequency.fromString(goal.frequency),
            reminderTime = goal.reminderTime,
            reminderDays = goal.reminderDays,
            progressPercent = calculateProgress(goal, checkIns, today),
            streak = calculateStreak(checkIns, today),
            checkedInToday = todayCheckIn != null
        )
    }

    companion object {
        fun calculateStreak(checkIns: List<GoalCheckInEntity>, today: LocalDate = LocalDate.now()): Int {
            val completedDates = checkIns
                .filter { it.completed }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .toSortedSet(compareByDescending { it })
                .toList()

            if (completedDates.isEmpty()) return 0
            if (completedDates.first() != today && completedDates.first() != today.minusDays(1)) return 0

            var streak = 1
            for (i in 0 until completedDates.size - 1) {
                if (completedDates[i].minusDays(1) == completedDates[i + 1]) {
                    streak++
                } else break
            }
            return streak
        }

        fun calculateProgress(
            goal: GoalEntity,
            checkIns: List<GoalCheckInEntity>,
            today: LocalDate = LocalDate.now()
        ): Int {
            val frequency = GoalFrequency.fromString(goal.frequency)
            val completedDates = checkIns
                .filter { it.completed }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .toSet()

            return when (frequency) {
                GoalFrequency.DAILY -> {
                    val month = YearMonth.from(today)
                    val daysElapsed = today.dayOfMonth
                    val checkInsThisMonth = completedDates.count { YearMonth.from(it) == month }
                    if (daysElapsed == 0) 0
                    else ((checkInsThisMonth.toFloat() / daysElapsed) * 100).toInt().coerceIn(0, 100)
                }

                GoalFrequency.WEEKLY -> {
                    val month = YearMonth.from(today)
                    val firstOfMonth = month.atDay(1)
                    val weeksElapsed = ((ChronoUnit.DAYS.between(firstOfMonth, today) / 7) + 1).toInt()
                    val weeksWithCheckIn = completedDates
                        .filter { YearMonth.from(it) == month }
                        .map { ChronoUnit.WEEKS.between(firstOfMonth, it) }
                        .toSet()
                        .size
                    if (weeksElapsed == 0) 0
                    else ((weeksWithCheckIn.toFloat() / weeksElapsed) * 100).toInt().coerceIn(0, 100)
                }

                GoalFrequency.MONTHLY -> {
                    val monthsElapsed = today.monthValue
                    val monthsWithCheckIn = completedDates
                        .filter { it.year == today.year }
                        .map { it.monthValue }
                        .toSet()
                        .size
                    if (monthsElapsed == 0) 0
                    else ((monthsWithCheckIn.toFloat() / monthsElapsed) * 100).toInt().coerceIn(0, 100)
                }
            }
        }
    }
}
