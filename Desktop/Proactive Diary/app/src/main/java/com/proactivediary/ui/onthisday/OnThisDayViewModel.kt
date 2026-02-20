package com.proactivediary.ui.onthisday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.media.ImageMetadata
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class MemoryEntry(
    val id: String,
    val title: String,
    val preview: String,
    @Deprecated("Mood feature removed") val mood: String? = null,
    val locationName: String?,
    val weatherCondition: String?,
    val weatherTemp: Double?,
    val images: List<ImageMetadata>,
    val dateFormatted: String,
    val yearsAgo: Int,
    val wordCount: Int
)

data class OnThisDayUiState(
    val entries: List<MemoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val todayFormatted: String = ""
)

@HiltViewModel
class OnThisDayViewModel @Inject constructor(
    private val entryDao: EntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnThisDayUiState())
    val uiState: StateFlow<OnThisDayUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    init {
        loadOnThisDayEntries()
    }

    private fun loadOnThisDayEntries() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val todayFormatted = today.format(DateTimeFormatter.ofPattern("MMMM d", Locale.US))

            _uiState.update { it.copy(todayFormatted = todayFormatted) }

            val allMemories = mutableListOf<MemoryEntry>()

            // Look back 1, 2, 3, 4, 5 years — and also 3, 6, 9 months
            val lookbackPeriods = listOf(
                3L to "months",
                6L to "months",
                9L to "months"
            ) + (1..5).map { it.toLong() to "years" }

            for ((amount, unit) in lookbackPeriods) {
                val targetDate = when (unit) {
                    "months" -> today.minusMonths(amount)
                    else -> today.minusYears(amount)
                }

                val startMs = targetDate.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val endMs = targetDate.plusDays(2).atStartOfDay(zone).toInstant().toEpochMilli() - 1

                val entries = entryDao.getEntriesBetween(startMs, endMs)
                for (entry in entries) {
                    val entryDate = Instant.ofEpochMilli(entry.createdAt)
                        .atZone(zone)
                        .toLocalDate()

                    val yearsAgo = when (unit) {
                        "months" -> 0 // We'll show "X months ago" differently
                        else -> amount.toInt()
                    }

                    allMemories.add(mapToMemoryEntry(entry, entryDate, yearsAgo, amount, unit))
                }
            }

            _uiState.update {
                it.copy(
                    entries = allMemories.sortedByDescending { m -> m.yearsAgo },
                    isLoading = false
                )
            }
        }
    }

    private fun mapToMemoryEntry(
        entity: EntryEntity,
        date: LocalDate,
        yearsAgo: Int,
        amount: Long,
        unit: String
    ): MemoryEntry {
        val plainText = entity.contentPlain ?: entity.content
        val preview = plainText.take(200).let {
            if (plainText.length > 200) "$it\u2026" else it
        }

        val images: List<ImageMetadata> = try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(entity.images, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val dateFormatted = when (unit) {
            "months" -> "${amount} months ago \u00B7 ${date.format(dateFormatter)}"
            else -> {
                val yearLabel = if (yearsAgo == 1) "year" else "years"
                "$yearsAgo $yearLabel ago \u00B7 ${date.format(dateFormatter)}"
            }
        }

        return MemoryEntry(
            id = entity.id,
            title = entity.title,
            preview = preview,
            mood = null,
            locationName = entity.locationName,
            weatherCondition = entity.weatherCondition,
            weatherTemp = entity.weatherTemp,
            images = images,
            dateFormatted = dateFormatted,
            yearsAgo = yearsAgo,
            wordCount = entity.wordCount
        )
    }
}
