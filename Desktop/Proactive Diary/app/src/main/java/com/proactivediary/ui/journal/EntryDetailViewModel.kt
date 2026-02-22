package com.proactivediary.ui.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.media.ImageMetadata
import com.proactivediary.data.media.ImageStorageManager
import com.proactivediary.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class EntryDetailUiState(
    val entryId: String = "",
    val title: String = "",
    val content: String = "",
    val contentHtml: String? = null,
    val tags: List<String> = emptyList(),
    val images: List<ImageMetadata> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val weatherTemp: Double? = null,
    val weatherCondition: String? = null,
    val weatherIcon: String? = null,
    val audioPath: String? = null,
    val wordCount: Int = 0,
    val dateHeader: String = "",
    val colorKey: String = "dark",
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
    val imageStorageManager: ImageStorageManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    private val themeKeys = listOf(
        "diary_color", "diary_form", "diary_canvas", "diary_details",
        "diary_mark_text", "diary_mark_position", "diary_mark_font", "font_size"
    )

    private val defaultFeatures = listOf("auto_save", "word_count", "date_header", "daily_quote")

    init {
        observeThemePreferences()
        loadEntry()
    }

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
                    colorKey = prefs["diary_color"] ?: "sky",
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
        if (entryId.isBlank()) {
            _uiState.value = _uiState.value.copy(isLoaded = true, isDeleted = true)
            return
        }
        viewModelScope.launch {
            val entry = entryRepository.getByIdSync(entryId)
            if (entry == null) {
                _uiState.value = _uiState.value.copy(isLoaded = true, isDeleted = true)
                return@launch
            }
            val tags = parseTags(entry.tags)
            val images = parseImages(entry.images)

            val zone = ZoneId.of("America/Los_Angeles")
            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
            val dateHeader = Instant.ofEpochMilli(entry.createdAt)
                .atZone(zone)
                .toLocalDate()
                .format(formatter)

            _uiState.value = _uiState.value.copy(
                entryId = entry.id,
                title = entry.title,
                content = entry.contentPlain ?: entry.content,
                contentHtml = entry.contentHtml,
                tags = tags,
                images = images,
                latitude = entry.latitude,
                longitude = entry.longitude,
                locationName = entry.locationName,
                weatherTemp = entry.weatherTemp,
                weatherCondition = entry.weatherCondition,
                weatherIcon = entry.weatherIcon,
                audioPath = entry.audioPath,
                wordCount = entry.wordCount,
                dateHeader = dateHeader,
                isLoaded = true
            )
        }
    }

    fun deleteEntry() {
        viewModelScope.launch {
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    entryRepository.delete(entryId)
                } catch (_: Exception) {
                    // Entry may already be deleted
                }
            }
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

    private fun parseImages(json: String): List<ImageMetadata> {
        return try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
