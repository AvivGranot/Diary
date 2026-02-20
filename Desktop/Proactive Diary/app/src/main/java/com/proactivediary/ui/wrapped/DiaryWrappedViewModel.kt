package com.proactivediary.ui.wrapped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * A single card in the Wrapped story.
 */
data class WrappedCard(
    val type: WrappedCardType,
    val headline: String,
    val subtitle: String = "",
    val bigNumber: String = "",
    val detail: String = "",
    val accentColorHex: Long = 0xFFF3EEE7 // Cream default
)

enum class WrappedCardType {
    INTRO,
    TOTAL_ENTRIES,
    TOTAL_WORDS,
    LONGEST_STREAK,
    FAVORITE_TIME,
    BUSIEST_DAY,
    TOP_LOCATION,
    LONGEST_ENTRY,
    FIRST_ENTRY,
    CLOSING
}

data class DiaryWrappedUiState(
    val cards: List<WrappedCard> = emptyList(),
    val isLoading: Boolean = true,
    val periodLabel: String = "",
    val userName: String = ""
)

@HiltViewModel
class DiaryWrappedViewModel @Inject constructor(
    private val entryDao: EntryDao,
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryWrappedUiState())
    val uiState: StateFlow<DiaryWrappedUiState> = _uiState.asStateFlow()

    private val zone = ZoneId.systemDefault()
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.US)
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.US)

    init {
        loadWrapped()
    }

    private fun loadWrapped() {
        viewModelScope.launch {
            // All processing on IO — streak calculation and aggregation are CPU-intensive
            val result = withContext(Dispatchers.IO) {
                val allEntries = entryDao.getAllSync()
                val userName = preferenceDao.getValue("diary_mark_text") ?: ""

                // Filter to current year only
                val currentYear = LocalDate.now().year
                val yearEntries = allEntries.filter { entry ->
                    Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate().year == currentYear
                }

                if (yearEntries.isEmpty()) {
                    return@withContext DiaryWrappedUiState(isLoading = false)
                }

                val cards = buildWrappedCards(yearEntries, userName, currentYear)

                val period = "January \u2013 December $currentYear"

                DiaryWrappedUiState(
                    cards = cards,
                    isLoading = false,
                    periodLabel = period,
                    userName = userName
                )
            }

            _uiState.value = result
        }
    }

    private fun buildWrappedCards(entries: List<EntryEntity>, userName: String, year: Int = LocalDate.now().year): List<WrappedCard> {
        val cards = mutableListOf<WrappedCard>()
        val totalEntries = entries.size
        val totalWords = entries.sumOf { it.wordCount }

        // 1. INTRO
        val greeting = if (userName.isNotBlank()) "Your $year story, $userName" else "Your $year story"
        cards.add(WrappedCard(
            type = WrappedCardType.INTRO,
            headline = greeting,
            subtitle = "A look back at your year of writing",
            accentColorHex = 0xFF313131
        ))

        // 2. TOTAL ENTRIES
        cards.add(WrappedCard(
            type = WrappedCardType.TOTAL_ENTRIES,
            headline = "You showed up",
            bigNumber = totalEntries.toString(),
            subtitle = "times",
            detail = entryNarrative(totalEntries),
            accentColorHex = 0xFF4A6741
        ))

        // 3. TOTAL WORDS
        val wordsFormatted = String.format(Locale.US, "%,d", totalWords)
        cards.add(WrappedCard(
            type = WrappedCardType.TOTAL_WORDS,
            headline = "You wrote",
            bigNumber = wordsFormatted,
            subtitle = "words",
            detail = wordNarrative(totalWords),
            accentColorHex = 0xFF5B4A6B
        ))

        // 4. LONGEST STREAK
        val streak = calculateLongestStreak(entries)
        if (streak > 1) {
            cards.add(WrappedCard(
                type = WrappedCardType.LONGEST_STREAK,
                headline = "Your longest streak",
                bigNumber = streak.toString(),
                subtitle = "consecutive days",
                detail = if (streak >= 30) "A month of daily practice. Remarkable."
                         else if (streak >= 14) "Two weeks straight. Real commitment."
                         else if (streak >= 7) "A full week of writing. That takes discipline."
                         else "$streak days in a row. Every streak starts somewhere.",
                accentColorHex = 0xFF6B5B3E
            ))
        }

        // 5. FAVORITE TIME
        val timeOfDay = entries.map {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalTime()
        }
        val timeDistribution = timeOfDay.groupBy { timeSlot(it) }.mapValues { it.value.size }
        val favoriteTime = timeDistribution.maxByOrNull { it.value }
        if (favoriteTime != null) {
            cards.add(WrappedCard(
                type = WrappedCardType.FAVORITE_TIME,
                headline = "You're a ${favoriteTime.key.lowercase()} writer",
                bigNumber = "${favoriteTime.value}",
                subtitle = "entries written in the ${favoriteTime.key.lowercase()}",
                detail = timeNarrative(favoriteTime.key),
                accentColorHex = timeColor(favoriteTime.key)
            ))
        }

        // 6. BUSIEST DAY
        val dayDistribution = entries.map {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate().dayOfWeek
        }.groupingBy { it }.eachCount()
        val busiestDay = dayDistribution.maxByOrNull { it.value }
        if (busiestDay != null) {
            val dayName = busiestDay.key.getDisplayName(TextStyle.FULL, Locale.US)
            cards.add(WrappedCard(
                type = WrappedCardType.BUSIEST_DAY,
                headline = "Your writing day",
                bigNumber = dayName,
                subtitle = "${busiestDay.value} entries on ${dayName}s",
                detail = dayNarrative(busiestDay.key),
                accentColorHex = 0xFF3E5B6B
            ))
        }

        // 7. TOP LOCATION
        val locations = entries.mapNotNull { it.locationName }
        if (locations.isNotEmpty()) {
            val locCounts = locations.groupingBy { it }.eachCount()
            val topLoc = locCounts.maxByOrNull { it.value }!!
            cards.add(WrappedCard(
                type = WrappedCardType.TOP_LOCATION,
                headline = "Your writing spot",
                bigNumber = topLoc.key,
                subtitle = "${topLoc.value} entries from here",
                detail = "Some of your best thinking happened at ${topLoc.key}.",
                accentColorHex = 0xFF4A6B5B
            ))
        }

        // 8. LONGEST ENTRY
        val longestEntry = entries.maxByOrNull { it.wordCount }
        if (longestEntry != null && longestEntry.wordCount > 50) {
            val date = Instant.ofEpochMilli(longestEntry.createdAt).atZone(zone).toLocalDate()
            cards.add(WrappedCard(
                type = WrappedCardType.LONGEST_ENTRY,
                headline = "Your deepest dive",
                bigNumber = "${longestEntry.wordCount}",
                subtitle = "words on ${date.format(dateFormatter)}",
                detail = if (longestEntry.title.isNotBlank())
                    "\u201C${longestEntry.title}\u201D \u2014 the day you had the most to say."
                else "The day you had the most to say.",
                accentColorHex = 0xFF5B6B4A
            ))
        }

        // 9. FIRST ENTRY
        val firstEntry = entries.minByOrNull { it.createdAt }
        if (firstEntry != null) {
            val date = Instant.ofEpochMilli(firstEntry.createdAt).atZone(zone).toLocalDate()
            val preview = (firstEntry.contentPlain ?: firstEntry.content).take(120)
            cards.add(WrappedCard(
                type = WrappedCardType.FIRST_ENTRY,
                headline = "Where it all began",
                subtitle = date.format(dateFormatter) + ", ${date.year}",
                detail = "\u201C${preview.trim()}\u201D",
                accentColorHex = 0xFF6B5B4A
            ))
        }

        // 10. CLOSING
        cards.add(WrappedCard(
            type = WrappedCardType.CLOSING,
            headline = "Keep going",
            subtitle = "Every word you write is a gift to your future self",
            detail = "$totalEntries entries. $wordsFormatted words. Your $year in words.",
            accentColorHex = 0xFF313131
        ))

        return cards
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun timeSlot(time: LocalTime): String = when (time.hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..20 -> "Evening"
        else -> "Night"
    }

    private fun timeColor(slot: String): Long = when (slot) {
        "Morning" -> 0xFF6B814A
        "Afternoon" -> 0xFF8B7A4A
        "Evening" -> 0xFF6B4A6B
        else -> 0xFF3E4A6B
    }

    private fun timeNarrative(slot: String): String = when (slot) {
        "Morning" -> "Before the day gets loud, you write. There\u2019s wisdom in that."
        "Afternoon" -> "Midday reflection. You process your life in real time."
        "Evening" -> "You end your days making sense of them. A beautiful ritual."
        else -> "Late-night thoughts. The quiet hours suit you."
    }

    private fun dayNarrative(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "Starting the week by putting thoughts to paper."
        DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY ->
            "Midweek writing. When life is busiest, you still make time."
        DayOfWeek.FRIDAY -> "Friday reflection. Closing the week with intention."
        DayOfWeek.SATURDAY -> "Saturday mornings and slow thinking."
        DayOfWeek.SUNDAY -> "Sunday writing. The week in review."
    }

    private fun entryNarrative(count: Int): String = when {
        count >= 365 -> "A full year of daily writing. You are a writer."
        count >= 100 -> "Over a hundred entries. This is more than a habit \u2014 it\u2019s a practice."
        count >= 50 -> "Fifty entries and counting. Real commitment to self-reflection."
        count >= 20 -> "Twenty entries is where patterns start to emerge."
        count >= 10 -> "You\u2019ve built a real body of reflection."
        else -> "Every journey begins with the first page."
    }

    private fun wordNarrative(words: Int): String = when {
        words >= 100_000 -> "That\u2019s a novel. Your life, in your own words."
        words >= 50_000 -> "50,000+ words. NaNoWriMo-level output \u2014 but about your real life."
        words >= 20_000 -> "A substantial body of writing. You have a lot to say."
        words >= 10_000 -> "10,000 words of honest self-expression."
        words >= 5_000 -> "5,000 words. Every one of them mattered."
        else -> "Every word is a step toward understanding yourself."
    }

    private fun calculateLongestStreak(entries: List<EntryEntity>): Int {
        if (entries.isEmpty()) return 0
        val dates = entries
            .map { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }
            .distinct()
            .sorted()

        var longest = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }
}
