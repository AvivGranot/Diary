package com.proactivediary.ui.write

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.domain.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class WriteUiState(
    val entryId: String = "",
    val title: String = "",
    val content: String = "",
    val mood: Mood? = null,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isLoaded: Boolean = false,
    val dateHeader: String = "",
    val colorKey: String = "cream",
    val form: String = "focused",
    val texture: String = "paper",
    val canvas: String = "lined",
    val fontSize: Int = 16,
    val features: List<String> = listOf("auto_save", "word_count", "date_header", "daily_quote"),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif",
    val isNewEntry: Boolean = true
)

@OptIn(FlowPreview::class)
@HiltViewModel
class WriteViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val preferenceDao: PreferenceDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    private val _contentFlow = MutableStateFlow("")
    private val gson = Gson()

    private val entryIdArg: String? = savedStateHandle.get<String>("entryId")

    init {
        loadThemePreferences()
        loadOrCreateEntry(entryIdArg)
        setupAutoSave()
    }

    private fun loadThemePreferences() {
        viewModelScope.launch {
            val colorKey = preferenceDao.get("diary_color")?.value ?: "cream"
            val form = preferenceDao.get("diary_form")?.value ?: "focused"
            val texture = preferenceDao.get("diary_texture")?.value ?: "paper"
            val canvas = preferenceDao.get("diary_canvas")?.value ?: "lined"
            val detailsJson = preferenceDao.get("diary_details")?.value
            val markText = preferenceDao.get("diary_mark_text")?.value ?: ""
            val markPosition = preferenceDao.get("diary_mark_position")?.value ?: "header"
            val markFont = preferenceDao.get("diary_mark_font")?.value ?: "serif"
            val fontSizePref = preferenceDao.get("font_size")?.value ?: "medium"

            val fontSize = when (fontSizePref) {
                "small" -> 14
                "large" -> 18
                else -> 16
            }

            val features: List<String> = if (detailsJson != null) {
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson(detailsJson, type)
                } catch (_: Exception) {
                    listOf("auto_save", "word_count", "date_header", "daily_quote")
                }
            } else {
                listOf("auto_save", "word_count", "date_header", "daily_quote")
            }

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
            val dateHeader = today.format(formatter)

            _uiState.value = _uiState.value.copy(
                colorKey = colorKey,
                form = form,
                texture = texture,
                canvas = canvas,
                fontSize = fontSize,
                features = features,
                markText = markText,
                markPosition = markPosition,
                markFont = markFont,
                dateHeader = dateHeader
            )
        }
    }

    fun loadOrCreateEntry(entryId: String?) {
        viewModelScope.launch {
            if (entryId != null) {
                val entry = entryRepository.getByIdSync(entryId)
                if (entry != null) {
                    val tags = parseTags(entry.tags)
                    _uiState.value = _uiState.value.copy(
                        entryId = entry.id,
                        title = entry.title,
                        content = entry.content,
                        mood = Mood.fromString(entry.mood),
                        tags = tags,
                        wordCount = computeWordCount(entry.content),
                        isLoaded = true,
                        isNewEntry = false
                    )
                    _contentFlow.value = entry.content
                }
            } else {
                val todayEntry = entryRepository.getTodayEntrySync()
                if (todayEntry != null) {
                    val tags = parseTags(todayEntry.tags)
                    _uiState.value = _uiState.value.copy(
                        entryId = todayEntry.id,
                        title = todayEntry.title,
                        content = todayEntry.content,
                        mood = Mood.fromString(todayEntry.mood),
                        tags = tags,
                        wordCount = computeWordCount(todayEntry.content),
                        isLoaded = true,
                        isNewEntry = false
                    )
                    _contentFlow.value = todayEntry.content
                } else {
                    val newId = UUID.randomUUID().toString()
                    _uiState.value = _uiState.value.copy(
                        entryId = newId,
                        title = "",
                        content = "",
                        mood = null,
                        tags = emptyList(),
                        wordCount = 0,
                        isLoaded = true,
                        isNewEntry = true
                    )
                    _contentFlow.value = ""
                }
            }
        }
    }

    private fun setupAutoSave() {
        viewModelScope.launch {
            _contentFlow
                .debounce(5000)
                .filter { it.isNotBlank() }
                .collect { text ->
                    saveEntry(text)
                }
        }
    }

    fun onContentChanged(newContent: String) {
        _uiState.value = _uiState.value.copy(
            content = newContent,
            wordCount = computeWordCount(newContent)
        )
        _contentFlow.value = newContent
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onMoodSelected(mood: Mood?) {
        val currentMood = _uiState.value.mood
        _uiState.value = _uiState.value.copy(
            mood = if (currentMood == mood) null else mood
        )
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun onTagsUpdated(tags: List<String>) {
        _uiState.value = _uiState.value.copy(tags = tags)
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun saveNow() {
        val state = _uiState.value
        if (state.content.isNotBlank() || state.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(state.content) }
        }
    }

    fun clearSaveError() {
        _uiState.value = _uiState.value.copy(saveError = null)
    }

    /** Returns true if there are unsaved changes worth warning about. */
    fun hasUnsavedContent(): Boolean {
        val state = _uiState.value
        return state.isNewEntry && (state.content.isNotBlank() || state.title.isNotBlank())
    }

    private suspend fun saveEntry(text: String) {
        val state = _uiState.value
        // Don't save completely empty entries
        if (text.isBlank() && state.title.isBlank()) return

        _uiState.value = state.copy(isSaving = true, saveError = null)

        try {
            val now = System.currentTimeMillis()
            val tagsJson = gson.toJson(state.tags)

            val entry = EntryEntity(
                id = state.entryId,
                title = state.title,
                content = text,
                mood = state.mood?.key,
                tags = tagsJson,
                wordCount = computeWordCount(text),
                createdAt = if (state.isNewEntry) now else {
                    entryRepository.getByIdSync(state.entryId)?.createdAt ?: now
                },
                updatedAt = now
            )

            if (state.isNewEntry) {
                entryRepository.insert(entry)
                _uiState.value = _uiState.value.copy(isNewEntry = false, isSaving = false)
            } else {
                entryRepository.update(entry)
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveError = "Failed to save entry. Please try again."
            )
        }
    }

    private fun parseTags(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun computeWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    }

    fun hasFeature(feature: String): Boolean {
        return feature in _uiState.value.features
    }
}
