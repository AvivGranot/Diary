package com.proactivediary.ui.journal

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class EntryDetailUiState(
    val entryId: String = "",
    val title: String = "",
    val content: String = "",
    val mood: Mood? = null,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val dateHeader: String = "",
    val colorKey: String = "cream",
    val form: String = "focused",
    val canvas: String = "lined",
    val fontSize: Int = 16,
    val features: List<String> = listOf("auto_save", "word_count", "date_header", "daily_quote"),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif",
    val isLoaded: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val preferenceDao: PreferenceDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    init {
        observeThemePreferences()
        loadEntry()
    }

    private val themeKeys = listOf(
        "diary_color", "diary_form", "diary_canvas", "diary_details",
        "diary_mark_text", "diary_mark_position", "diary_mark_font", "font_size"
    )

    private val defaultFeatures = listOf("auto_save", "word_count", "date_header", "daily_quote")

    private fun observeThemePreferences() {
        viewModelScope.launch {
            preferenceDao.observeBatch(themeKeys).collect { entities ->
                val prefs = entities.associate { it.key to it.value }

                val detailsJson = prefs["diary_details"]
                val features: List<String> = if (detailsJson != null) {
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson(detailsJson, type)
                    } catch (_: Exception) { defaultFeatures }
                } else { defaultFeatures }

                _uiState.value = _uiState.value.copy(
                    colorKey = prefs["diary_color"] ?: "cream",
                    form = prefs["diary_form"] ?: "focused",
                    canvas = prefs["diary_canvas"] ?: "lined",
                    fontSize = when (prefs["font_size"]) {
                        "small" -> 14; "large" -> 18; else -> 16
                    },
                    features = features,
                    markText = prefs["diary_mark_text"] ?: "",
                    markPosition = prefs["diary_mark_position"] ?: "header",
                    markFont = prefs["diary_mark_font"] ?: "serif"
                )
            }
        }
    }

    private fun loadEntry() {
        viewModelScope.launch {
            val entry = entryRepository.getByIdSync(entryId) ?: return@launch
            val tags = parseTags(entry.tags)

            val zone = ZoneId.systemDefault()
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
            val dateHeader = Instant.ofEpochMilli(entry.createdAt)
                .atZone(zone)
                .toLocalDate()
                .format(formatter)

            _uiState.value = _uiState.value.copy(
                entryId = entry.id,
                title = entry.title,
                content = entry.content,
                mood = Mood.fromString(entry.mood),
                tags = tags,
                wordCount = entry.wordCount,
                dateHeader = dateHeader,
                isLoaded = true
            )
        }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            entryRepository.delete(entryId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
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
}
