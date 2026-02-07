package com.proactivediary.domain.search

interface SearchEngine {
    fun isDateQuery(query: String): Boolean
    fun buildFtsQuery(userInput: String): String
    fun parseDateRange(query: String): Pair<Long, Long>?
}
