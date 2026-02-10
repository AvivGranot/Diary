package com.proactivediary.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.GoalEntity
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.data.repository.GoalRepository
import com.proactivediary.data.repository.ReminderRepository
import com.proactivediary.notifications.NotificationService
import com.proactivediary.ui.components.formatTimeForStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingGoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val reminderRepository: ReminderRepository,
    private val preferenceDao: PreferenceDao,
    private val notificationService: NotificationService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    suspend fun saveOnboardingData(
        goals: List<GoalInput>,
        reminders: List<ReminderInput>,
        fallbackEnabled: Boolean
    ) {
        withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()

                // Save goals
                for (input in goals) {
                    if (input.title.isBlank()) continue

                    val daysJson = "[${input.selectedDays.sorted().joinToString(",")}]"
                    val goal = GoalEntity(
                        id = UUID.randomUUID().toString(),
                        title = input.title,
                        frequency = input.frequency.name.lowercase(),
                        reminderTime = formatTimeForStorage(input.hour, input.minute),
                        reminderDays = daysJson,
                        createdAt = now,
                        updatedAt = now
                    )
                    goalRepository.insertGoal(goal)
                    notificationService.scheduleGoalReminder(goal)
                }

                // Save writing reminders
                for (input in reminders) {
                    if (!input.enabled) continue

                    val reminder = WritingReminderEntity(
                        id = UUID.randomUUID().toString(),
                        time = formatTimeForStorage(input.hour, input.minute),
                        days = "[0,1,2,3,4,5,6]",
                        isActive = true,
                        fallbackEnabled = fallbackEnabled,
                        label = input.label
                    )
                    reminderRepository.insert(reminder)
                    notificationService.scheduleWritingReminder(reminder)
                    if (fallbackEnabled) {
                        notificationService.scheduleFallbackCheck(reminder)
                    }
                }

                // Mark onboarding as completed only after all saves succeed
                preferenceDao.insert(PreferenceEntity("onboarding_completed", "true"))
                preferenceDao.insert(PreferenceEntity("goals_onboarding_completed", "true"))
                analyticsService.logOnboardingGoalsCompleted(goals.size)
            } catch (e: Exception) {
                // Still mark completed to avoid stuck onboarding, but goals/reminders may be partial
                try {
                    preferenceDao.insert(PreferenceEntity("onboarding_completed", "true"))
                    preferenceDao.insert(PreferenceEntity("goals_onboarding_completed", "true"))
                } catch (_: Exception) { }
            }
        }
    }

    suspend fun skipOnboarding() {
        withContext(Dispatchers.IO) {
            preferenceDao.insert(PreferenceEntity("onboarding_completed", "true"))
            preferenceDao.insert(PreferenceEntity("goals_onboarding_completed", "true"))
            analyticsService.logOnboardingGoalsSkipped()
        }
    }
}
