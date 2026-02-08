package com.proactivediary.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.data.repository.ReminderRepository
import com.proactivediary.notifications.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderManagementViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    val reminders: StateFlow<List<WritingReminderEntity>> = reminderRepository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(time: String, days: String, fallbackEnabled: Boolean) {
        viewModelScope.launch {
            val reminder = WritingReminderEntity(
                id = UUID.randomUUID().toString(),
                time = time,
                days = days,
                isActive = true,
                fallbackEnabled = fallbackEnabled,
                label = "Write in your diary"
            )
            reminderRepository.insert(reminder)
            notificationService.scheduleWritingReminder(reminder)
            if (fallbackEnabled) {
                notificationService.scheduleFallbackCheck(reminder)
            }
        }
    }

    fun toggleReminder(reminder: WritingReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(isActive = !reminder.isActive)
            reminderRepository.update(updated)
            if (updated.isActive) {
                notificationService.scheduleWritingReminder(updated)
                if (updated.fallbackEnabled) {
                    notificationService.scheduleFallbackCheck(updated)
                }
            } else {
                notificationService.cancelReminder(updated.id)
            }
        }
    }

    fun deleteReminder(reminder: WritingReminderEntity) {
        viewModelScope.launch {
            notificationService.cancelReminder(reminder.id)
            reminderRepository.delete(reminder.id)
        }
    }

    fun updateReminder(reminder: WritingReminderEntity, time: String, days: String, fallbackEnabled: Boolean) {
        viewModelScope.launch {
            notificationService.cancelReminder(reminder.id)
            val updated = reminder.copy(
                time = time,
                days = days,
                fallbackEnabled = fallbackEnabled
            )
            reminderRepository.update(updated)
            if (updated.isActive) {
                notificationService.scheduleWritingReminder(updated)
                if (fallbackEnabled) {
                    notificationService.scheduleFallbackCheck(updated)
                }
            }
        }
    }
}
