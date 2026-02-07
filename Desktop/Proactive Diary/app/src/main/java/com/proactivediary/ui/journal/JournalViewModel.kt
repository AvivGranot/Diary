package com.proactivediary.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.domain.model.Mood
import com.proactivediary.domain.search.SearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class JournalUiState(
    val entries: List<DiaryCardData> = emptyList(),
    val groupedEntries: Map<String, List<DiaryCardData>> = emptyMap(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isSearchEmpty: Boolean = false
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val searchEngine: SearchEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadAllEntries()
    }

    private fun loadAllEntries() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            entryRepository.getAllEntries()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmpty = true
                    )
                }
                .collect { entities ->
                    val cards = entities.map { it.toCardData() }
                    val grouped = groupByDate(cards)
                    _uiState.value = _uiState.value.copy(
                        entries = cards,
                        groupedEntries = grouped,
                        isLoading = false,
                        isEmpty = cards.isEmpty(),
                        isSearchEmpty = false
                    )
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(isSearching = false, isSearchEmpty = false)
            loadAllEntries()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search input
            performSearch(query)
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false,
            isSearchEmpty = false
        )
        loadAllEntries()
    }

    private fun performSearch(query: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isSearching = true)

        if (searchEngine.isDateQuery(query)) {
            // Date-based search
            val dateRange = searchEngine.parseDateRange(query)
            if (dateRange != null) {
                loadJob = viewModelScope.launch {
                    entryRepository.getEntriesByDateRange(dateRange.first, dateRange.second)
                        .catch {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                isSearchEmpty = true,
                                entries = emptyList(),
                                groupedEntries = emptyMap()
                            )
                        }
                        .collect { entities ->
                            val cards = entities.map { it.toCardData() }
                            val grouped = groupByDate(cards)
                            _uiState.value = _uiState.value.copy(
                                entries = cards,
                                groupedEntries = grouped,
                                isSearching = false,
                                isSearchEmpty = cards.isEmpty(),
                                isEmpty = false
                            )
                        }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    isSearchEmpty = true,
                    entries = emptyList(),
                    groupedEntries = emptyMap()
                )
            }
        } else {
            // Content FTS search
            val ftsQuery = searchEngine.buildFtsQuery(query)
            loadJob = viewModelScope.launch {
                entryRepository.searchContent(ftsQuery)
                    .catch {
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            isSearchEmpty = true,
                            entries = emptyList(),
                            groupedEntries = emptyMap()
                        )
                    }
                    .collect { entities ->
                        val cards = entities.map { it.toCardData() }
                        val grouped = groupByDate(cards)
                        _uiState.value = _uiState.value.copy(
                            entries = cards,
                            groupedEntries = grouped,
                            isSearching = false,
                            isSearchEmpty = cards.isEmpty(),
                            isEmpty = false
                        )
                    }
            }
        }
    }

    private fun EntryEntity.toCardData(): DiaryCardData {
        val tagsList: List<String> = try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(tags, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        return DiaryCardData(
            id = id,
            title = title,
            content = content,
            mood = Mood.fromString(mood),
            tags = tagsList,
            wordCount = wordCount,
            createdAt = createdAt
        )
    }

    private fun groupByDate(cards: List<DiaryCardData>): Map<String, List<DiaryCardData>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

        return cards.groupBy { card ->
            val date = Instant.ofEpochMilli(card.createdAt)
                .atZone(zone)
                .toLocalDate()

            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> date.format(formatter)
            }
        }
    }
}
