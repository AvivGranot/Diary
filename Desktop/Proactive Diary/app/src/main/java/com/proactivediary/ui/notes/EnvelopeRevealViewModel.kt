package com.proactivediary.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.social.Note
import com.proactivediary.data.social.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnvelopeRevealState(
    val note: Note? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class EnvelopeRevealViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val noteId: String = savedStateHandle["noteId"] ?: ""

    private val _state = MutableStateFlow(EnvelopeRevealState())
    val state: StateFlow<EnvelopeRevealState> = _state.asStateFlow()

    init {
        if (noteId.isNotBlank()) {
            viewModelScope.launch {
                // Find the note from the real-time stream
                val notes = notesRepository.observeReceivedNotes().first()
                val note = notes.find { it.id == noteId }
                _state.value = EnvelopeRevealState(note = note, isLoading = false)

                // Mark as read
                if (note != null && note.status != "read") {
                    notesRepository.markAsRead(noteId)
                }
            }
        } else {
            _state.value = EnvelopeRevealState(note = null, isLoading = false)
        }
    }
}
