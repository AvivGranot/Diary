package com.proactivediary.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.ai.AIChatService
import com.proactivediary.data.ai.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoDeeperUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isApiKeyMissing: Boolean = false,
    val entryId: String = "",
    val error: String? = null
)

@HiltViewModel
class GoDeeperViewModel @Inject constructor(
    private val chatService: AIChatService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoDeeperUiState())
    val uiState: StateFlow<GoDeeperUiState> = _uiState.asStateFlow()

    // EntryId can come from nav args (SavedStateHandle) or be set directly
    private var entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    init {
        if (entryId.isNotBlank()) {
            _uiState.update { it.copy(entryId = entryId) }
            checkApiKey()
        }
    }

    /**
     * Initialize with an entryId when used from a composable (not via navigation).
     * Guards against double-init when LaunchedEffect re-fires.
     */
    fun initialize(id: String) {
        if (id.isBlank()) return
        if (id == entryId && (_uiState.value.messages.isNotEmpty() || _uiState.value.isLoading)) return
        entryId = id
        _uiState.update { it.copy(entryId = id, messages = emptyList(), error = null, isLoading = false) }
        checkApiKey()
    }

    private fun checkApiKey() {
        viewModelScope.launch {
            val key = chatService.getApiKey()
            if (key.isNullOrBlank()) {
                _uiState.update { it.copy(isApiKeyMissing = true) }
            } else {
                // Auto-start with the AI's opening question
                startConversation()
            }
        }
    }

    private fun startConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val response = chatService.goDeeper(
                entryId = entryId,
                userMessage = "I just finished writing this entry. Help me reflect deeper.",
                conversationHistory = emptyList()
            )

            if (response != null) {
                _uiState.update {
                    it.copy(
                        messages = listOf(ChatMessage(text = response, isUser = false)),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Couldn't connect to AI. Check your API key in Settings."
                    )
                }
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
                error = null
            )
        }

        viewModelScope.launch {
            val response = chatService.goDeeper(
                entryId = entryId,
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
                        error = "Something went wrong. Try again."
                    )
                }
            }
        }
    }
}
