package com.proactivediary.ui.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.domain.suggestions.Suggestion
import com.proactivediary.domain.suggestions.SuggestionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuggestionsUiState(
    val forYouSuggestions: List<Suggestion> = emptyList(),
    val recentSuggestions: List<Suggestion> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: Int = 0
)

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val suggestionEngine: SuggestionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuggestionsUiState())
    val uiState: StateFlow<SuggestionsUiState> = _uiState.asStateFlow()

    init {
        loadSuggestions()
    }

    fun loadSuggestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val forYou = suggestionEngine.generateSuggestions()
                val recent = suggestionEngine.getRecentSuggestions()
                _uiState.value = _uiState.value.copy(
                    forYouSuggestions = forYou,
                    recentSuggestions = recent,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }
}
