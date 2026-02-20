package com.proactivediary.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.ui.journal.AIInsightData
import com.proactivediary.ui.journal.DiaryCardData
import com.proactivediary.ui.write.WritingPrompts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class DiaryHomeUiState(
    val todayPrompt: String = WritingPrompts.dailyPrompt(),
    val groupedEntries: Map<String, List<DiaryCardData>> = emptyMap(),
    val aiInsight: AIInsightData = AIInsightData(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@HiltViewModel
class DiaryHomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val insightDao: InsightDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryHomeUiState())
    val uiState: StateFlow<DiaryHomeUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadEntries()
        loadAIInsight()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            entryRepository.getAllEntries()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmpty = true
                    )
                }
                .collect { entities ->
                    val cards = entities.take(50).map { it.toCardData() }
                    val grouped = groupByDate(cards)
                    _uiState.value = _uiState.value.copy(
                        groupedEntries = grouped,
                        isLoading = false,
                        isEmpty = cards.isEmpty()
                    )
                }
        }
    }

    private fun loadAIInsight() {
        viewModelScope.launch {
            insightDao.getLatestInsight().collect { insight ->
                if (insight != null) {
                    val themes: List<String> = try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson(insight.themes, type) ?: emptyList()
                    } catch (_: Exception) { emptyList() }

                    val prompts: List<String> = try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson(insight.promptSuggestions, type) ?: emptyList()
                    } catch (_: Exception) { emptyList() }

                    _uiState.value = _uiState.value.copy(
                        aiInsight = AIInsightData(
                            summary = insight.summary,
                            themes = themes,
                            moodTrend = null,
                            promptSuggestions = prompts,
                            isAvailable = true,
                            isLocked = false
                        )
                    )
                }
            }
        }
    }

    private fun EntryEntity.toCardData(): DiaryCardData {
        val tagsList: List<String> = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(tags, type) ?: emptyList()
        } catch (_: Exception) { emptyList() }

        val imageCount = try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val list: List<Map<String, Any>>? = gson.fromJson(images, type)
            list?.size ?: 0
        } catch (_: Exception) { 0 }

        return DiaryCardData(
            id = id,
            title = title,
            content = contentPlain ?: content,
            tags = tagsList,
            wordCount = wordCount,
            createdAt = createdAt,
            imageCount = imageCount,
            mood = null
        )
    }

    private fun groupByDate(cards: List<DiaryCardData>): Map<String, List<DiaryCardData>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Use a LinkedHashMap to preserve insertion order (Today first, then Yesterday, then older)
        val result = linkedMapOf<String, MutableList<DiaryCardData>>()

        cards.forEach { card ->
            val date = Instant.ofEpochMilli(card.createdAt)
                .atZone(zone)
                .toLocalDate()

            val key = when (date) {
                today -> "TODAY"
                yesterday -> "YESTERDAY"
                else -> date.format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                ).uppercase(Locale.getDefault())
            }

            result.getOrPut(key) { mutableListOf() }.add(card)
        }

        return result
    }
}
