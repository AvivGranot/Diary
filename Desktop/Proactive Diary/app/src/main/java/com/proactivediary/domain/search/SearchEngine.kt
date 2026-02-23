package com.proactivediary.domain.search

data class NaturalLanguageQuery(
    val type: QueryType,
    val value: String
)

enum class QueryType {
    TEXT,       // Regular FTS content search
    DATE,       // Date range query
    MOOD,       // Mood filter (great, good, okay, bad, awful)
    TAG,        // Tag filter
    LOCATION    // Location name filter
}

interface SearchEngine {
    fun isDateQuery(query: String): Boolean
    fun buildFtsQuery(userInput: String): String
    fun parseDateRange(query: String): Pair<Long, Long>?
    /** Parse a natural language query into a structured query type. */
    fun parseNaturalQuery(query: String): NaturalLanguageQuery
}
