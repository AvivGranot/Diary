package com.proactivediary.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.social.ContactMatcher
import com.proactivediary.data.social.ContentModerator
import com.proactivediary.data.social.MatchedContact
import com.proactivediary.data.social.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComposeNoteState(
    val content: String = "",
    val wordCount: Int = 0,
    val recipientName: String? = null,
    val recipientId: String? = null,
    val recipientPhone: String? = null,
    val recipientEmail: String? = null,
    val isRecipientOnApp: Boolean? = null,
    val isResolving: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val moderationError: String? = null,
    val isSent: Boolean = false
)

@HiltViewModel
class ComposeNoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val contactMatcher: ContactMatcher,
    private val contentModerator: ContentModerator
) : ViewModel() {

    private val _state = MutableStateFlow(ComposeNoteState())
    val state: StateFlow<ComposeNoteState> = _state.asStateFlow()

    fun updateContent(text: String) {
        val words = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        if (words <= MAX_WORDS) {
            _state.value = _state.value.copy(
                content = text,
                wordCount = words,
                moderationError = null,
                error = null
            )
        }
    }

    fun onContactSelected(name: String?, phone: String?, email: String?) {
        _state.value = _state.value.copy(
            recipientName = name ?: "Friend",
            recipientPhone = phone,
            recipientEmail = email,
            isResolving = true,
            isRecipientOnApp = null
        )

        viewModelScope.launch {
            val match = contactMatcher.resolveContact(phone = phone, email = email)
            _state.value = _state.value.copy(
                isResolving = false,
                isRecipientOnApp = match != null,
                recipientId = match?.userId
            )
        }
    }

    fun sendNote() {
        val current = _state.value
        if (current.content.isBlank()) return

        // Client-side moderation check
        val modResult = contentModerator.check(current.content)
        if (!modResult.isAllowed) {
            _state.value = current.copy(moderationError = modResult.reason)
            return
        }

        val recipientId = current.recipientId
        if (recipientId == null) {
            // Recipient not on app — should trigger invite flow instead
            return
        }

        _state.value = current.copy(isSending = true, error = null)

        viewModelScope.launch {
            val result = notesRepository.sendNote(recipientId, current.content)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSending = false, isSent = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = e.message ?: "Failed to send note"
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null, moderationError = null)
    }

    companion object {
        const val MAX_WORDS = 100
    }
}
