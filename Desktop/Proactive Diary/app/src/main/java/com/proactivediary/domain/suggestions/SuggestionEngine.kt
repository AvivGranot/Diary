package com.proactivediary.domain.suggestions

interface SuggestionEngine {
    suspend fun generateSuggestions(): List<Suggestion>
    suspend fun getRecentSuggestions(): List<Suggestion>
}
