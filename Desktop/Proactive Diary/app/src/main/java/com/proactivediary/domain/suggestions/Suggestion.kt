package com.proactivediary.domain.suggestions

data class Suggestion(
    val id: String,
    val type: SuggestionType,
    val title: String,
    val subtitle: String,
    val prompt: String,
    val iconName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceLabel: String? = null
)

enum class SuggestionType {
    LOCATION,
    WEATHER,
    STREAK,
    TIME_OF_DAY,
    ON_THIS_DAY,
    MOOD_PATTERN,
    WRITING_HABIT,
    REFLECTION
}
