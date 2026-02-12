package com.proactivediary.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.StreakRepository
import com.proactivediary.domain.search.SearchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

data class OnThisDayEntry(
    val entryId: String,
    val firstLine: String,
    val label: String, // e.g. "One month ago, you wrote:"
    val daysAgo: Long
)

data class JournalUiState(
    val entries: List<DiaryCardData> = emptyList(),
    val groupedEntries: Map<String, List<DiaryCardData>> = emptyMap(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isSearchEmpty: Boolean = false,
    val insights: JournalInsights = JournalInsights(),
    val writingDays: Set<LocalDate> = emptySet(),
    val weeklyDigest: WeeklyDigest = WeeklyDigest(),
    val onThisDay: OnThisDayEntry? = null,
    val hasMoreEntries: Boolean = false,
    val isLoadingMore: Boolean = false,
    val aiInsight: AIInsightData = AIInsightData()
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val searchEngine: SearchEngine,
    private val streakRepository: StreakRepository,
    private val analyticsService: com.proactivediary.analytics.AnalyticsService,
    private val insightDao: InsightDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var currentOffset = 0
    private var totalEntryCount = 0

    companion object {
        const val PAGE_SIZE = 50
    }

    private var searchUsed = false

    init {
        loadInitialEntries()
        loadInsights()
        loadWeeklyDigest()
        loadOnThisDay()
        loadAIInsight()
        logJournalOpened()
    }

    private fun logJournalOpened() {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) { entryRepository.getTotalEntryCount() }
            analyticsService.logJournalViewed(count, searchUsed = false)
        }
    }

    private fun loadInitialEntries() {
        loadJob?.cancel()
        currentOffset = 0
        loadJob = viewModelScope.launch {
            try {
                val (entries, total) = withContext(Dispatchers.IO) {
                    val page = entryRepository.getPage(PAGE_SIZE, 0)
                    val total = entryRepository.getTotalCount()
                    Pair(page, total)
                }
                totalEntryCount = total
                currentOffset = entries.size
                val cards = entries.map { it.toCardData() }
                val grouped = groupByDate(cards)
                _uiState.value = _uiState.value.copy(
                    entries = cards,
                    groupedEntries = grouped,
                    isLoading = false,
                    isEmpty = cards.isEmpty(),
                    isSearchEmpty = false,
                    hasMoreEntries = currentOffset < totalEntryCount
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEmpty = true
                )
            }
        }

        // Also observe for real-time updates (new entries, edits, deletions)
        viewModelScope.launch {
            entryRepository.getAllEntries()
                .catch { /* already handled above */ }
                .collect { entities ->
                    totalEntryCount = entities.size
                    // Always re-map visible entries so title/content updates are reflected
                    val visibleCount = currentOffset.coerceAtMost(entities.size)
                    val cards = entities.take(visibleCount).map { it.toCardData() }
                    val grouped = groupByDate(cards)
                    _uiState.value = _uiState.value.copy(
                        entries = cards,
                        groupedEntries = grouped,
                        isEmpty = cards.isEmpty(),
                        hasMoreEntries = visibleCount < totalEntryCount
                    )
                }
        }
    }

    fun loadMoreEntries() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreEntries) return
        if (_uiState.value.searchQuery.isNotBlank()) return

        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        viewModelScope.launch {
            try {
                val moreEntries = withContext(Dispatchers.IO) {
                    entryRepository.getPage(PAGE_SIZE, currentOffset)
                }
                currentOffset += moreEntries.size
                val newCards = moreEntries.map { it.toCardData() }
                val allCards = _uiState.value.entries + newCards
                val grouped = groupByDate(allCards)
                _uiState.value = _uiState.value.copy(
                    entries = allCards,
                    groupedEntries = grouped,
                    isLoadingMore = false,
                    hasMoreEntries = currentOffset < totalEntryCount
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.value = _uiState.value.copy(isSearching = false, isSearchEmpty = false)
            loadInitialEntries()
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
        loadInitialEntries()
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
            imageCount = imageCount
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

    private fun loadInsights() {
        viewModelScope.launch {
            val (insights, writingDays) = withContext(Dispatchers.IO) {
                val allEntries = entryRepository.getAllSync()
                val zone = ZoneId.systemDefault()

                // Writing days for heatmap
                val writingDays = allEntries.map { entry ->
                    Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate()
                }.toSet()

                // Most active day of week
                val dayOfWeekCounts = allEntries.map { entry ->
                    Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate().dayOfWeek
                }.groupingBy { it }.eachCount()
                val mostActiveDay = dayOfWeekCounts.maxByOrNull { it.value }?.key?.getDisplayName(
                    JavaTextStyle.FULL, Locale.getDefault()
                )

                val totalWords = allEntries.sumOf { it.wordCount }
                val avgWords = if (allEntries.isNotEmpty()) totalWords / allEntries.size else 0

                // Use StreakRepository instead of duplicate calculation
                val streak = streakRepository.calculateWritingStreak()

                val insights = JournalInsights(
                    totalEntries = allEntries.size,
                    totalWords = totalWords,
                    averageWordsPerEntry = avgWords,
                    currentStreak = streak,
                    mostActiveDay = mostActiveDay?.plus("s") // "Tuesdays"
                )

                Pair(insights, writingDays)
            }

            _uiState.value = _uiState.value.copy(
                insights = insights,
                writingDays = writingDays
            )
        }
    }

    private fun loadWeeklyDigest() {
        viewModelScope.launch {
            val today = LocalDate.now()
            // Show digest on Sundays only
            val isSunday = today.dayOfWeek == DayOfWeek.SUNDAY
            if (!isSunday) return@launch

            val digest = withContext(Dispatchers.IO) {
                val lastSunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                val weekStart = lastSunday.minusDays(6) // Monday of the week being reviewed

                val zone = ZoneId.systemDefault()
                val startMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
                val endMs = lastSunday.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

                val allEntries = entryRepository.getAllSync()
                val weekEntries = allEntries.filter { it.createdAt in startMs..endMs }

                if (weekEntries.isEmpty()) return@withContext null

                val totalWords = weekEntries.sumOf { it.wordCount }

                WeeklyDigest(
                    entryCount = weekEntries.size,
                    totalWords = totalWords,
                    isVisible = true
                )
            }

            if (digest != null) {
                _uiState.value = _uiState.value.copy(weeklyDigest = digest)
            }
        }
    }

    private fun loadOnThisDay() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val today = LocalDate.now()
                val daysAgoList = listOf(30L, 90L, 180L, 365L)
                val labels = mapOf(
                    30L to "One month ago, you wrote:",
                    90L to "Three months ago, you wrote:",
                    180L to "Six months ago, you wrote:",
                    365L to "One year ago, you wrote:"
                )

                for (daysAgo in daysAgoList) {
                    val pastDate = today.minusDays(daysAgo)
                    val entry = entryRepository.getEntryForDate(pastDate)
                    if (entry != null) {
                        val firstLine = (entry.contentPlain ?: entry.content).lines()
                            .firstOrNull { it.isNotBlank() }
                            ?.take(120)
                            ?: continue
                        return@withContext OnThisDayEntry(
                            entryId = entry.id,
                            firstLine = firstLine,
                            label = labels[daysAgo] ?: "You wrote:",
                            daysAgo = daysAgo
                        )
                    }
                }
                null
            }

            if (result != null) {
                _uiState.value = _uiState.value.copy(onThisDay = result)
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
                            moodTrend = insight.moodTrend,
                            promptSuggestions = prompts,
                            isAvailable = true,
                            isLocked = false
                        )
                    )
                }
            }
        }
    }

    fun dismissWeeklyDigest() {
        _uiState.value = _uiState.value.copy(
            weeklyDigest = _uiState.value.weeklyDigest.copy(isVisible = false)
        )
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            entryRepository.delete(entryId)
        }
    }
}
