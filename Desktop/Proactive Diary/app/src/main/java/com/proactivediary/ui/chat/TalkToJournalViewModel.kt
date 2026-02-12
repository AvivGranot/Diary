package com.proactivediary.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.ai.AIChatService
import com.proactivediary.data.ai.ChatMessage
import com.proactivediary.data.db.dao.EntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TalkToJournalUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isApiKeyMissing: Boolean = false,
    val entryCount: Int = 0,
    val error: String? = null,
    val suggestedQuestions: List<String> = emptyList()
)

@HiltViewModel
class TalkToJournalViewModel @Inject constructor(
    private val chatService: AIChatService,
    private val entryDao: EntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TalkToJournalUiState())
    val uiState: StateFlow<TalkToJournalUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            val key = chatService.getApiKey()
            val count = entryDao.getTotalCount()

            val suggestions = listOf(
                "What patterns do you see in my writing?",
                "When am I happiest?",
                "What topics keep coming back?",
                "How has my mood changed over time?",
                "What should I reflect on more?"
            )

            _uiState.update {
                it.copy(
                    isApiKeyMissing = key.isNullOrBlank(),
                    entryCount = count,
                    suggestedQuestions = suggestions
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        val currentMessages = _uiState.value.messages

        _uiState.update {
            it.copy(
                messages = currentMessages + userMessage,
                isLoading = true,
                error = null,
                suggestedQuestions = emptyList() // Hide suggestions after first message
            )
        }

        viewModelScope.launch {
            val response = chatService.talkToJournal(
                userMessage = text,
                conversationHistory = currentMessages
            )

            if (response != null) {
                val aiMessage = ChatMessage(text = response, isUser = false)
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Couldn\u2019t connect. Check your API key in Settings."
                    )
                }
            }
        }
    }
}
