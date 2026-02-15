package com.proactivediary.ui.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.domain.suggestions.Suggestion
import com.proactivediary.domain.suggestions.SuggestionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.proactivediary.domain.suggestions.SuggestionType
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SuggestionsUiState(
    val forYouSuggestions: List<Suggestion> = emptyList(),
    val recentSuggestions: List<Suggestion> = emptyList(),
    val reflectionPrompts: List<Suggestion> = emptyList(),
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
                    reflectionPrompts = generateReflectionPrompts(),
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

    private fun generateReflectionPrompts(): List<Suggestion> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val prompts = mutableListOf<Suggestion>()

        // Time-based
        if (hour < 12) {
            prompts.add(Suggestion(id = "reflect_1", type = SuggestionType.REFLECTION, title = "What are you most looking forward to today?", subtitle = "Morning reflection", prompt = "What are you most looking forward to today?\n\n", iconName = "auto_awesome"))
            prompts.add(Suggestion(id = "reflect_2", type = SuggestionType.REFLECTION, title = "What intention do you want to set?", subtitle = "Morning reflection", prompt = "What intention do you want to set for today?\n\n", iconName = "auto_awesome"))
        } else if (hour < 17) {
            prompts.add(Suggestion(id = "reflect_1", type = SuggestionType.REFLECTION, title = "What surprised you today?", subtitle = "Afternoon check-in", prompt = "What has surprised you so far today?\n\n", iconName = "auto_awesome"))
            prompts.add(Suggestion(id = "reflect_2", type = SuggestionType.REFLECTION, title = "A moment worth remembering", subtitle = "Afternoon check-in", prompt = "What moment today deserves to be remembered?\n\n", iconName = "auto_awesome"))
        } else {
            prompts.add(Suggestion(id = "reflect_1", type = SuggestionType.REFLECTION, title = "What would you redo?", subtitle = "Evening reflection", prompt = "What would you do differently if you could redo today?\n\n", iconName = "auto_awesome"))
            prompts.add(Suggestion(id = "reflect_2", type = SuggestionType.REFLECTION, title = "Gratitude check", subtitle = "Evening reflection", prompt = "What are you grateful for right now?\n\n", iconName = "auto_awesome"))
        }

        // Generic
        prompts.add(Suggestion(id = "reflect_3", type = SuggestionType.REFLECTION, title = "Patterns you're noticing", subtitle = "Self-discovery", prompt = "What pattern in your life are you just starting to notice?\n\n", iconName = "auto_awesome"))
        prompts.add(Suggestion(id = "reflect_4", type = SuggestionType.REFLECTION, title = "A conversation that stayed", subtitle = "Relationships", prompt = "What conversation has stayed with you recently, and why?\n\n", iconName = "auto_awesome"))

        return prompts
    }
}
