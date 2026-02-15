package com.proactivediary.ui.discover

import androidx.lifecycle.ViewModel
import com.proactivediary.data.discover.DiscoverContent
import com.proactivediary.domain.model.DiscoverEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DiscoverUiState(
    val entries: List<DiscoverEntry> = emptyList(),
    val todayEntries: List<DiscoverEntry> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DiscoverViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadTodayEntries()
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

    fun filterByCategory(category: String?) {
        _uiState.update { state ->
            val filtered = if (category == null) {
                state.todayEntries
            } else {
                // When filtering by category, show all entries in that category
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
