package com.proactivediary.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val entryDays: Set<Int> = emptySet()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEntryDaysForMonth(_uiState.value.currentMonth)
    }

    fun setMonth(month: LocalDate) {
        _uiState.value = _uiState.value.copy(currentMonth = month.withDayOfMonth(1))
        loadEntryDaysForMonth(month)
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        setMonth(newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        setMonth(newMonth)
    }

    private fun loadEntryDaysForMonth(month: LocalDate) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val startOfMonth = month.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfMonth = month.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            entryRepository.getEntryDatesInRange(startOfMonth, endOfMonth).collect { timestamps ->
                val days = timestamps.map { timestamp ->
                    Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate().dayOfMonth
                }.toSet()
                _uiState.value = _uiState.value.copy(entryDays = days)
            }
        }
    }
}
