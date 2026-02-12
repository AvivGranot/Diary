package com.proactivediary.domain.suggestions

import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.GoalCheckInDao
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class LocalSuggestionEngine @Inject constructor(
    private val entryDao: EntryDao,
    private val goalCheckInDao: GoalCheckInDao
) : SuggestionEngine {

    override suspend fun generateSuggestions(): List<Suggestion> = coroutineScope {
        val locationDeferred = async { generateLocationSuggestions() }
        val weatherDeferred = async { generateWeatherSuggestions() }
        val streakDeferred = async { generateStreakSuggestions() }
        val timeDeferred = async { generateTimeOfDaySuggestions() }
        val onThisDayDeferred = async { generateOnThisDaySuggestions() }
        val moodDeferred = async { generateMoodPatternSuggestions() }
        val habitDeferred = async { generateWritingHabitSuggestions() }

        val all = locationDeferred.await() +
            weatherDeferred.await() +
            streakDeferred.await() +
            timeDeferred.await() +
            onThisDayDeferred.await() +
            moodDeferred.await() +
            habitDeferred.await()

        val dailySeed = LocalDate.now().dayOfYear.toLong()
        all.shuffled(Random(dailySeed)).take(6)
    }

    override suspend fun getRecentSuggestions(): List<Suggestion> {
        return generateSuggestions().sortedByDescending { it.timestamp }
    }

    // ── a) Location-based suggestions ──────────────────────────────────

    private suspend fun generateLocationSuggestions(): List<Suggestion> {
        val now = System.currentTimeMillis()
        val fourteenDaysAgo = now - 14L * 24 * 60 * 60 * 1000
        val recentEntries = entryDao.getEntriesBetween(fourteenDaysAgo, now)

        val distinctLocations = recentEntries
            .mapNotNull { it.locationName }
            .filter { it.isNotBlank() }
            .distinct()
            .take(2)

        return distinctLocations.map { location ->
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = SuggestionType.LOCATION,
                title = "You were at $location",
                subtitle = "Reflect on your visit",
                prompt = "You were at $location. What made that visit memorable?",
                iconName = "location",
                sourceLabel = location
            )
        }
    }

    // ── b) Weather-based suggestions ───────────────────────────────────

    private suspend fun generateWeatherSuggestions(): List<Suggestion> {
        val entries = entryDao.getAllSync()
        val mostRecent = entries.firstOrNull() ?: return emptyList()
        val condition = mostRecent.weatherCondition?.lowercase() ?: return emptyList()

        val (title, prompt) = when {
            condition.contains("clear") || condition.contains("sunny") ->
                "Sunny vibes" to "The sun is out today. What are you grateful for?"
            condition.contains("rain") || condition.contains("drizzle") ->
                "Rainy reflections" to "Rainy days are perfect for reflection. What's on your mind?"
            condition.contains("cloud") || condition.contains("overcast") ->
                "Quiet skies" to "A quiet, overcast day. What's something you've been thinking about?"
            condition.contains("snow") ->
                "Snowy thoughts" to "Snow changes the world outside. What's changing in your world?"
            else ->
                "Weather check" to "How does today's weather make you feel?"
        }

        return listOf(
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = SuggestionType.WEATHER,
                title = title,
                subtitle = condition.replaceFirstChar { it.uppercase() },
                prompt = prompt,
                iconName = "weather",
                sourceLabel = condition
            )
        )
    }

    // ── c) Streak-based suggestions ────────────────────────────────────

    private suspend fun generateStreakSuggestions(): List<Suggestion> {
        val streak = calculateWritingStreak()
        val totalEntries = entryDao.getTotalCount()

        return when {
            streak >= 3 -> listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.STREAK,
                    title = "$streak-day streak!",
                    subtitle = "Keep the momentum going",
                    prompt = "You're on a $streak-day writing streak! What's keeping you inspired?",
                    iconName = "streak",
                    sourceLabel = "$streak days"
                )
            )
            streak == 0 && totalEntries > 0 -> listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.STREAK,
                    title = "Welcome back",
                    subtitle = "Pick up where you left off",
                    prompt = "It's been a few days since you last wrote. Pick up where you left off.",
                    iconName = "streak"
                )
            )
            else -> emptyList()
        }
    }

    // ── d) Time-of-day suggestions ─────────────────────────────────────

    private suspend fun generateTimeOfDaySuggestions(): List<Suggestion> {
        val hour = LocalTime.now().hour
        val dayOfYear = LocalDate.now().dayOfYear
        val pickIndex = dayOfYear % 2 // deterministic daily pick

        val (title, prompts) = when (hour) {
            in 5..11 -> "Good morning" to listOf(
                "What are you looking forward to today?",
                "Set an intention for this morning."
            )
            in 12..16 -> "Afternoon check-in" to listOf(
                "How is your day going so far?",
                "What's something that surprised you today?"
            )
            in 17..21 -> "Evening reflection" to listOf(
                "What was the highlight of your day?",
                "What are you proud of from today?"
            )
            else -> "Late night thoughts" to listOf(
                "As the day winds down, what's on your mind?",
                "What would you like to remember from today?"
            )
        }

        val prompt = prompts[pickIndex]

        return listOf(
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = SuggestionType.TIME_OF_DAY,
                title = title,
                subtitle = "A prompt for right now",
                prompt = prompt,
                iconName = "time"
            )
        )
    }

    // ── e) On This Day suggestions ─────────────────────────────────────

    private suspend fun generateOnThisDaySuggestions(): List<Suggestion> {
        val today = LocalDate.now()
        val oneYearAgo = today.minusYears(1)
        val zone = ZoneId.systemDefault()

        val startMs = oneYearAgo.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = oneYearAgo.plusDays(2).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val pastEntries = entryDao.getEntriesBetween(startMs, endMs)
        if (pastEntries.isEmpty()) return emptyList()

        val entry = pastEntries.first()
        val text = (entry.contentPlain ?: entry.content).take(50)
        val preview = if ((entry.contentPlain ?: entry.content).length > 50) "$text..." else text

        return listOf(
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = SuggestionType.ON_THIS_DAY,
                title = "On this day, a year ago",
                subtitle = preview,
                prompt = "A year ago, you wrote: '$preview'. How have things changed?",
                iconName = "calendar"
            )
        )
    }

    // ── f) Mood pattern suggestions ────────────────────────────────────

    private suspend fun generateMoodPatternSuggestions(): List<Suggestion> {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
        val recentEntries = entryDao.getEntriesBetween(sevenDaysAgo, now)

        val moods = recentEntries.mapNotNull { it.mood }
        if (moods.size < 2) return emptyList()

        val moodCounts = moods.groupingBy { it }.eachCount()
        val dominantMood = moodCounts.maxByOrNull { it.value }

        return if (dominantMood != null && dominantMood.value >= 3) {
            listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.MOOD_PATTERN,
                    title = "Mood pattern",
                    subtitle = "You've been feeling ${dominantMood.key} lately",
                    prompt = "You've been feeling ${dominantMood.key} lately. What's contributing to that?",
                    iconName = "mood",
                    sourceLabel = dominantMood.key
                )
            )
        } else if (moodCounts.size >= 3) {
            listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.MOOD_PATTERN,
                    title = "Mood shifts",
                    subtitle = "Your mood has been changing",
                    prompt = "Your mood has been shifting this week. What's influencing your emotions?",
                    iconName = "mood"
                )
            )
        } else {
            emptyList()
        }
    }

    // ── g) Writing habit suggestions ───────────────────────────────────

    private suspend fun generateWritingHabitSuggestions(): List<Suggestion> {
        val totalEntries = entryDao.getTotalCount()
        if (totalEntries == 0) return emptyList()

        val totalWords = entryDao.getTotalWordCountSync()
        val avgWordCount = totalWords / totalEntries

        return when {
            totalEntries > 10 && avgWordCount > 200 -> listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.WRITING_HABIT,
                    title = "Deep thinker",
                    subtitle = "Average entry: $avgWordCount words",
                    prompt = "You're a deep thinker \u2014 your average entry is $avgWordCount words. What's driving that depth?",
                    iconName = "habit",
                    sourceLabel = "$totalEntries entries"
                )
            )
            totalEntries > 10 -> listOf(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.WRITING_HABIT,
                    title = "Committed writer",
                    subtitle = "$totalEntries entries and counting",
                    prompt = "You've written $totalEntries entries so far. What topic keeps coming back?",
                    iconName = "habit",
                    sourceLabel = "$totalEntries entries"
                )
            )
            else -> emptyList()
        }
    }

    // ── Helper: calculate consecutive writing streak ───────────────────

    private suspend fun calculateWritingStreak(): Int {
        var streak = 0
        var date = LocalDate.now()
        val zone = ZoneId.systemDefault()
        while (true) {
            val startOfDay = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            val count = entryDao.countEntriesForDay(startOfDay, endOfDay)
            if (count > 0) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
