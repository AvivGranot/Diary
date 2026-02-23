package com.proactivediary.domain.suggestions

import android.util.Log
import com.proactivediary.data.db.dao.ActivitySignalDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.ActivitySignalEntity
import com.proactivediary.data.repository.EntryRepository
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A suggestion card surfaced to the user as a journaling prompt.
 */
data class Suggestion(
    val id: String,
    val type: String,           // "moment", "on_this_day", "time_of_day", "streak", "random"
    val title: String,
    val prompt: String,
    val previewText: String?,
    val photoUri: String?,
    val locationName: String?,
    val timestamp: Long
)

/**
 * Generates contextual journaling suggestions by combining activity signals
 * (photos, locations) clustered into moments with time-of-day prompts,
 * streak prompts, on-this-day memories, and curated prompts.
 *
 * Priority: moment-based > on-this-day > streak > time-based > random prompt
 */
@Singleton
class EnhancedSuggestionEngine @Inject constructor(
    private val activitySignalDao: ActivitySignalDao,
    private val entryRepository: EntryRepository,
    private val momentClusterer: MomentClusterer,
    private val preferenceDao: PreferenceDao
) {
    companion object {
        private const val TAG = "EnhancedSuggestionEngine"
    }

    /**
     * Generates up to [limit] suggestions, mixing moment-based and prompt-based cards.
     */
    suspend fun generateSuggestions(limit: Int = 5): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()

        try {
            // 1. Moment-based suggestions (highest priority)
            suggestions.addAll(generateMomentSuggestions())

            // 2. On-this-day suggestions
            suggestions.addAll(generateOnThisDaySuggestions())

            // 3. Streak-based suggestions
            suggestions.addAll(generateStreakSuggestions())

            // 4. Mood-aware suggestions (adapts to last recorded mood)
            suggestions.addAll(generateMoodAwareSuggestions())

            // 5. Time-of-day prompt suggestions
            suggestions.addAll(generateTimeOfDaySuggestions())

            // 6. Fill remaining with random curated prompts
            if (suggestions.size < limit) {
                val remaining = limit - suggestions.size
                suggestions.addAll(generateRandomPromptSuggestions(remaining))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error generating suggestions: ${e.message}")
            // Fallback: always return at least one prompt
            if (suggestions.isEmpty()) {
                suggestions.add(createTimeOfDaySuggestion())
            }
        }

        return suggestions.take(limit)
    }

    /**
     * Clusters unconsumed activity signals into moments and generates
     * a suggestion card for each rich moment.
     */
    private suspend fun generateMomentSuggestions(): List<Suggestion> {
        // Filter unconsumed signals by privacy preferences
        val photosEnabled = preferenceDao.getSync("privacy_photos")?.value != "false"
        val locationEnabled = preferenceDao.getSync("privacy_location")?.value != "false"

        val unconsumed = activitySignalDao.getUnconsumedSync().filter { signal ->
            when (signal.type) {
                "photo" -> photosEnabled
                "location" -> locationEnabled
                else -> true
            }
        }
        if (unconsumed.isEmpty()) return emptyList()

        val moments = momentClusterer.clusterSignals(unconsumed)

        return moments.take(3).map { moment ->
            val photoSignal = moment.signals.firstOrNull { it.type == "photo" }
            val photoUri = photoSignal?.let { extractPhotoUri(it) }
            val locationName = extractLocationSummary(moment)
            val photoCount = moment.signals.count { it.type == "photo" }

            val title = when {
                photoCount > 1 && locationName != null ->
                    "$photoCount photos near $locationName"
                photoCount > 1 ->
                    "$photoCount photos from ${formatTimeRange(moment.startTime, moment.endTime)}"
                locationName != null ->
                    "A moment near $locationName"
                else ->
                    "A moment worth capturing"
            }

            val prompt = when {
                photoCount > 0 && locationName != null ->
                    "You took photos near $locationName. What were you doing? How did it feel?"
                photoCount > 0 ->
                    "You captured some moments recently. What was the story behind them?"
                locationName != null ->
                    "You were near $locationName. What brought you there?"
                else ->
                    "Something happened recently. Take a moment to write about it."
            }

            Suggestion(
                id = UUID.randomUUID().toString(),
                type = "moment",
                title = title,
                prompt = prompt,
                previewText = null,
                photoUri = photoUri,
                locationName = locationName,
                timestamp = moment.startTime
            )
        }
    }

    /**
     * Looks for entries written on this date in previous years.
     */
    private suspend fun generateOnThisDaySuggestions(): List<Suggestion> {
        val today = LocalDate.now()
        val suggestions = mutableListOf<Suggestion>()

        // Check the last 5 years
        for (yearsAgo in 1..5) {
            val pastDate = today.minusYears(yearsAgo.toLong())

            val entry = try {
                entryRepository.getEntryForDate(pastDate)
            } catch (_: Exception) {
                null
            }

            if (entry != null) {
                val contentPreview = (entry.contentPlain ?: entry.content)
                    .take(120)
                    .let { if (it.length == 120) "$it..." else it }

                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = "on_this_day",
                        title = "On this day, $yearsAgo year${if (yearsAgo > 1) "s" else ""} ago",
                        prompt = "You wrote this ${yearsAgo} year${if (yearsAgo > 1) "s" else ""} ago. How have things changed?",
                        previewText = contentPreview,
                        photoUri = null,
                        locationName = entry.locationName,
                        timestamp = entry.createdAt
                    )
                )
            }
        }

        return suggestions.take(1) // At most one on-this-day card
    }

    /**
     * Generates a streak-based motivational suggestion if the user has an active streak.
     */
    private suspend fun generateStreakSuggestions(): List<Suggestion> {
        val totalEntries = try {
            entryRepository.getTotalEntryCount()
        } catch (_: Exception) {
            0
        }

        if (totalEntries == 0) return emptyList()

        // Check if the user has written today
        val hasWrittenToday = try {
            entryRepository.hasEntryToday()
        } catch (_: Exception) {
            false
        }

        if (hasWrittenToday) return emptyList()

        val prompt = when {
            totalEntries >= 100 ->
                "You have written $totalEntries entries. Keep the momentum going with today's reflection."
            totalEntries >= 30 ->
                "Over $totalEntries entries and counting. What will today's story be?"
            totalEntries >= 7 ->
                "You have been writing consistently. A few words today keeps the habit alive."
            else ->
                "Every entry adds to your story. What is on your mind today?"
        }

        return listOf(
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = "streak",
                title = "Continue your writing journey",
                prompt = prompt,
                previewText = null,
                photoUri = null,
                locationName = null,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Generates a mood-aware suggestion based on the user's most recent mood.
     */
    private suspend fun generateMoodAwareSuggestions(): List<Suggestion> {
        val lastMood = try {
            entryRepository.getLatestMood()
        } catch (_: Exception) {
            null
        }

        if (lastMood.isNullOrBlank()) return emptyList()

        val title = when (lastMood) {
            "great", "good" -> "Feeling good? Capture why"
            "okay" -> "Middle-of-the-road day"
            "bad", "awful" -> "A gentle prompt for right now"
            else -> "A prompt for you"
        }

        return listOf(
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = "mood",
                title = title,
                prompt = PromptLibrary.getRandomForMood(lastMood),
                previewText = null,
                photoUri = null,
                locationName = null,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Creates a suggestion based on the time of day using the prompt library.
     */
    private fun generateTimeOfDaySuggestions(): List<Suggestion> {
        return listOf(createTimeOfDaySuggestion())
    }

    private fun createTimeOfDaySuggestion(): Suggestion {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val title = when (hour) {
            in 5..11 -> "Morning reflection"
            in 12..16 -> "Afternoon check-in"
            in 17..21 -> "Evening wind-down"
            else -> "Late-night thoughts"
        }

        return Suggestion(
            id = UUID.randomUUID().toString(),
            type = "time_of_day",
            title = title,
            prompt = PromptLibrary.getRandomForTimeOfDay(),
            previewText = null,
            photoUri = null,
            locationName = null,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Fills remaining slots with random curated prompts from various categories.
     */
    private fun generateRandomPromptSuggestions(count: Int): List<Suggestion> {
        val categories = listOf("gratitude", "growth", "creative")
        return (1..count).map { i ->
            val category = categories[i % categories.size]
            Suggestion(
                id = UUID.randomUUID().toString(),
                type = "random",
                title = category.replaceFirstChar { it.uppercase() } + " prompt",
                prompt = PromptLibrary.getRandomByCategory(category),
                previewText = null,
                photoUri = null,
                locationName = null,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun extractPhotoUri(signal: ActivitySignalEntity): String? {
        return try {
            val json = JSONObject(signal.data)
            json.optString("uri", "").ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractLocationSummary(moment: Moment): String? {
        // For now, return coordinates as a compact label.
        // A full implementation would reverse-geocode, but we avoid network calls here.
        val lat = moment.latitude ?: return null
        val lng = moment.longitude ?: return null
        return "%.2f, %.2f".format(lat, lng)
    }

    private fun formatTimeRange(startMs: Long, endMs: Long): String {
        val zone = ZoneId.systemDefault()
        val startTime = Instant.ofEpochMilli(startMs).atZone(zone).toLocalTime()
        val endTime = Instant.ofEpochMilli(endMs).atZone(zone).toLocalTime()

        val startHour = startTime.hour
        val endHour = endTime.hour

        return when {
            startHour == endHour -> formatHour(startHour)
            else -> "${formatHour(startHour)} - ${formatHour(endHour)}"
        }
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12 AM"
            hour < 12 -> "$hour AM"
            hour == 12 -> "12 PM"
            else -> "${hour - 12} PM"
        }
    }
}
