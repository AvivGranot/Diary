package com.proactivediary.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.entities.EntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * A single month's mood data point.
 */
data class MoodDataPoint(
    val month: String,           // "Jan", "Feb", etc.
    val yearMonth: String,       // "2025-01"
    val averageMoodScore: Float, // 1-5 scale (awful=1, great=5)
    val entryCount: Int,
    val dominantMood: String
)

/**
 * A word that appears frequently in entries.
 */
data class TopicWord(
    val word: String,
    val count: Int,
    val weight: Float // 0-1 normalized
)

/**
 * A location with its associated mood.
 */
data class LocationMood(
    val name: String,
    val entryCount: Int,
    val averageMoodScore: Float,
    val dominantMood: String
)

/**
 * Writing pattern by time of day.
 */
data class TimePattern(
    val label: String, // "Morning", "Afternoon", "Evening", "Night"
    val entryCount: Int,
    val averageMoodScore: Float,
    val percentage: Float
)

data class ThemeEvolutionUiState(
    val moodTimeline: List<MoodDataPoint> = emptyList(),
    val topicWords: List<TopicWord> = emptyList(),
    val locationMoods: List<LocationMood> = emptyList(),
    val timePatterns: List<TimePattern> = emptyList(),
    val totalEntries: Int = 0,
    val totalWords: Int = 0,
    val averageMoodScore: Float = 0f,
    val moodTrend: String = "",  // "improving", "stable", "declining"
    val isLoading: Boolean = true
)

@HiltViewModel
class ThemeEvolutionViewModel @Inject constructor(
    private val entryDao: EntryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeEvolutionUiState())
    val uiState: StateFlow<ThemeEvolutionUiState> = _uiState.asStateFlow()

    private val zone = ZoneId.systemDefault()
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.US)
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)

    // Common words to exclude from topic analysis
    private val stopWords = setOf(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
        "by", "is", "was", "are", "were", "be", "been", "being", "have", "has", "had",
        "do", "does", "did", "will", "would", "could", "should", "may", "might", "can",
        "shall", "it", "its", "i", "my", "me", "we", "our", "us", "you", "your", "he",
        "she", "his", "her", "they", "them", "their", "this", "that", "these", "those",
        "what", "which", "who", "whom", "when", "where", "how", "why", "all", "each",
        "every", "both", "few", "more", "most", "other", "some", "such", "no", "not",
        "only", "own", "same", "so", "than", "too", "very", "just", "about", "also",
        "if", "then", "because", "as", "until", "while", "from", "up", "out", "into",
        "through", "during", "before", "after", "above", "below", "between", "under",
        "again", "further", "once", "here", "there", "am", "been", "being", "having",
        "doing", "going", "like", "really", "much", "don't", "didn't", "doesn't", "won't",
        "wasn", "weren", "haven", "hasn", "hadn", "wouldn", "couldn", "shouldn", "isn",
        "aren", "ain", "get", "got", "make", "made", "let", "know", "think", "went",
        "come", "came", "thing", "things", "something", "anything", "nothing", "everything",
        "today", "yesterday", "tomorrow", "now", "still", "back", "even", "well", "way",
        "around", "though", "always", "never", "one", "two", "three", "new", "old",
        "long", "little", "big", "first", "last", "next", "good", "great", "right"
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // All data processing on IO — topic word extraction is CPU-heavy
            val result = withContext(Dispatchers.IO) {
                val entries = entryDao.getAllSync()
                if (entries.isEmpty()) return@withContext null

                val moodTimeline = buildMoodTimeline(entries)
                val topicWords = buildTopicWords(entries)
                val locationMoods = buildLocationMoods(entries)
                val timePatterns = buildTimePatterns(entries)
                val totalEntries = entries.size
                val totalWords = entries.sumOf { it.wordCount }

                val moods = entries.mapNotNull { it.mood }
                val avgMood = if (moods.isNotEmpty()) {
                    moods.map { moodScore(it) }.average().toFloat()
                } else 0f

                val moodTrend = calculateMoodTrend(moodTimeline)

                ThemeEvolutionUiState(
                    moodTimeline = moodTimeline,
                    topicWords = topicWords,
                    locationMoods = locationMoods,
                    timePatterns = timePatterns,
                    totalEntries = totalEntries,
                    totalWords = totalWords,
                    averageMoodScore = avgMood,
                    moodTrend = moodTrend,
                    isLoading = false
                )
            }

            _uiState.update { result ?: it.copy(isLoading = false) }
        }
    }

    private fun buildMoodTimeline(entries: List<EntryEntity>): List<MoodDataPoint> {
        val grouped = entries
            .filter { it.mood != null }
            .groupBy { entry ->
                val date = Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate()
                date.format(yearMonthFormatter)
            }
            .toSortedMap()

        return grouped.map { (yearMonth, monthEntries) ->
            val moods = monthEntries.mapNotNull { it.mood }
            val avgScore = moods.map { moodScore(it) }.average().toFloat()
            val dominant = moods.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: ""
            val date = monthEntries.first().let {
                Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate()
            }

            MoodDataPoint(
                month = date.format(monthFormatter),
                yearMonth = yearMonth,
                averageMoodScore = avgScore,
                entryCount = monthEntries.size,
                dominantMood = dominant
            )
        }
    }

    private fun buildTopicWords(entries: List<EntryEntity>): List<TopicWord> {
        val wordCounts = mutableMapOf<String, Int>()

        for (entry in entries) {
            val text = (entry.contentPlain ?: entry.content).lowercase()
            val words = text.split(Regex("[^a-z']+"))
                .filter { it.length in 3..20 && it !in stopWords }

            for (word in words) {
                wordCounts[word] = (wordCounts[word] ?: 0) + 1
            }
        }

        // Get top 30 words, normalize weights
        val topWords = wordCounts.entries
            .sortedByDescending { it.value }
            .take(30)

        if (topWords.isEmpty()) return emptyList()

        val maxCount = topWords.first().value.toFloat()
        return topWords.map { (word, count) ->
            TopicWord(
                word = word,
                count = count,
                weight = count / maxCount
            )
        }
    }

    private fun buildLocationMoods(entries: List<EntryEntity>): List<LocationMood> {
        val locationEntries = entries
            .filter { it.locationName != null && it.mood != null }
            .groupBy { it.locationName!! }

        return locationEntries
            .filter { it.value.size >= 2 } // Only locations with 2+ entries
            .map { (location, locEntries) ->
                val moods = locEntries.mapNotNull { it.mood }
                val avgScore = moods.map { moodScore(it) }.average().toFloat()
                val dominant = moods.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: ""

                LocationMood(
                    name = location,
                    entryCount = locEntries.size,
                    averageMoodScore = avgScore,
                    dominantMood = dominant
                )
            }
            .sortedByDescending { it.averageMoodScore }
            .take(10)
    }

    private fun buildTimePatterns(entries: List<EntryEntity>): List<TimePattern> {
        val timeSlots = entries.groupBy { entry ->
            val hour = Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalTime().hour
            when (hour) {
                in 5..11 -> "Morning"
                in 12..16 -> "Afternoon"
                in 17..20 -> "Evening"
                else -> "Night"
            }
        }

        val total = entries.size.toFloat()

        return listOf("Morning", "Afternoon", "Evening", "Night").map { slot ->
            val slotEntries = timeSlots[slot] ?: emptyList()
            val moods = slotEntries.mapNotNull { it.mood }
            val avgScore = if (moods.isNotEmpty()) moods.map { moodScore(it) }.average().toFloat() else 0f

            TimePattern(
                label = slot,
                entryCount = slotEntries.size,
                averageMoodScore = avgScore,
                percentage = if (total > 0) slotEntries.size / total else 0f
            )
        }
    }

    private fun calculateMoodTrend(timeline: List<MoodDataPoint>): String {
        if (timeline.size < 2) return "stable"

        val recentHalf = timeline.takeLast(timeline.size / 2)
        val olderHalf = timeline.take(timeline.size / 2)

        val recentAvg = recentHalf.map { it.averageMoodScore }.average()
        val olderAvg = olderHalf.map { it.averageMoodScore }.average()

        val diff = recentAvg - olderAvg
        return when {
            diff > 0.3 -> "improving"
            diff < -0.3 -> "declining"
            else -> "stable"
        }
    }

    private fun moodScore(mood: String): Float = when (mood) {
        "great" -> 5f
        "good" -> 4f
        "okay" -> 3f
        "bad" -> 2f
        "awful" -> 1f
        else -> 3f
    }
}
