package com.proactivediary.ui.notes

import androidx.lifecycle.ViewModel
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.social.Note
import com.proactivediary.data.social.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NoteInboxViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    val analyticsService: AnalyticsService
) : ViewModel() {

    val notes: Flow<List<Note>> = notesRepository.observeReceivedNotes()
    val unreadCount: Flow<Int> = notesRepository.observeUnreadCount()
}
