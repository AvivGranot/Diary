package com.proactivediary.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.discover.DiscoverContent
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.domain.model.DiscoverEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class WelcomeBackState(
    val isVisible: Boolean = false,
    val entryCount: Int = 0,
    val totalWords: Int = 0
)

data class DiscoverUiState(
    val entries: List<DiscoverEntry> = emptyList(),
    val todayEntries: List<DiscoverEntry> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true,
    val welcomeBack: WelcomeBackState = WelcomeBackState()
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    companion object {
        private const val LAPSED_THRESHOLD_DAYS = 7L
    }

    init {
        loadTodayEntries()
        checkLapsedUser()
    }

    private fun loadTodayEntries() {
        val today = DiscoverContent.getEntriesForToday()
        _uiState.update {
            it.copy(
                entries = today,
                todayEntries = today,
                isLoading = false
            )
        }
    }

    private fun checkLapsedUser() {
        viewModelScope.launch {
            val isLapsed = withContext(Dispatchers.IO) {
                val lastEntryMs = entryRepository.getLastEntryTimestamp()
                if (lastEntryMs != null) {
                    val lastEntryDate = Instant.ofEpochMilli(lastEntryMs)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val daysSince = LocalDate.now().toEpochDay() - lastEntryDate.toEpochDay()
                    daysSince >= LAPSED_THRESHOLD_DAYS
                } else {
                    false
                }
            }

            if (isLapsed) {
                val (entryCount, totalWords) = withContext(Dispatchers.IO) {
                    Pair(
                        entryRepository.getTotalEntryCountSync(),
                        entryRepository.getTotalWordCountSync()
                    )
                }
                _uiState.update {
                    it.copy(
                        welcomeBack = WelcomeBackState(
                            isVisible = true,
                            entryCount = entryCount,
                            totalWords = totalWords
                        )
                    )
                }
            }
        }
    }

    fun dismissWelcomeBack() {
        _uiState.update {
            it.copy(welcomeBack = it.welcomeBack.copy(isVisible = false))
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.update { state ->
            val filtered = if (category == null) {
                state.todayEntries
            } else {
                DiscoverContent.getByCategory(category)
            }
            state.copy(
                entries = filtered,
                selectedCategory = category
            )
        }
    }

    fun refresh() {
        val fresh = DiscoverContent.getRandomSelection(8)
        _uiState.update {
            it.copy(
                entries = fresh,
                todayEntries = fresh,
                selectedCategory = null
            )
        }
    }
}
