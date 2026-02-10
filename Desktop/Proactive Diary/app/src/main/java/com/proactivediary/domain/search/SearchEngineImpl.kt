package com.proactivediary.domain.search

import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class SearchEngineImpl @Inject constructor() : SearchEngine {

    private val stopWords = setOf(
        "the", "a", "an", "is", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would",
        "could", "should", "may", "might", "shall", "can", "to", "of",
        "in", "for", "on", "with", "at", "by", "from", "as", "into",
        "about", "like", "through", "after", "over", "between", "out",
        "against", "during", "without", "before", "under", "around",
        "among", "i", "me", "my", "we", "our", "you", "your", "he",
        "she", "it", "they", "them", "this", "that", "these", "those",
        "and", "but", "or", "not", "no", "so", "if", "when", "what",
        "which", "who", "how", "all", "each", "every", "both", "few",
        "more", "most", "some", "any", "just", "very", "really",
        "today", "went", "got", "also", "then", "than"
    )

    private val monthNames = mapOf(
        "january" to Month.JANUARY, "february" to Month.FEBRUARY,
        "march" to Month.MARCH, "april" to Month.APRIL,
        "may" to Month.MAY, "june" to Month.JUNE,
        "july" to Month.JULY, "august" to Month.AUGUST,
        "september" to Month.SEPTEMBER, "october" to Month.OCTOBER,
        "november" to Month.NOVEMBER, "december" to Month.DECEMBER
    )

    override fun isDateQuery(query: String): Boolean {
        val lower = query.lowercase().trim()
        val datePatterns = listOf(
            Regex("\\d{4}-\\d{2}-\\d{2}"),
            Regex("\\d{1,2}/\\d{1,2}/\\d{2,4}"),
            // "may" alone is too common as a verb — only match it when followed by a day number
            Regex("\\b(january|february|march|april|june|july|august|september|october|november|december)\\b(\\s+\\d{1,2})?(,?\\s*\\d{4})?"),
            Regex("\\bmay\\s+\\d{1,2}(,?\\s*\\d{4})?"),
            Regex("^yesterday$"),
            Regex("^last\\s+(week|month|year)$")
        )
        return datePatterns.any { it.containsMatchIn(lower) }
    }

    override fun buildFtsQuery(userInput: String): String {
        // Strip FTS4 metacharacters that would cause syntax errors
        val sanitized = userInput.replace(Regex("[\"()*^]"), "")
        if (sanitized.isBlank()) return "IMPOSSIBLE_MATCH_TOKEN"
        val words = sanitized.lowercase().trim().split("\\s+".toRegex())
        val filtered = words.filter { it !in stopWords && it.length > 1 }
        if (filtered.isEmpty()) return sanitized.lowercase().trim() + "*"
        return filtered.joinToString(" AND ") { "$it*" }
    }

    override fun parseDateRange(query: String): Pair<Long, Long>? {
        val lower = query.lowercase().trim()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        // "yesterday"
        if (lower == "yesterday") {
            val yesterday = today.minusDays(1)
            return dayRange(yesterday, zone)
        }

        // "last week"
        if (lower == "last week") {
            val start = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val end = start.plusDays(6)
            return Pair(
                start.atStartOfDay(zone).toInstant().toEpochMilli(),
                end.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            )
        }

        // "last month"
        if (lower == "last month") {
            val lastMonth = today.minusMonths(1)
            val start = lastMonth.with(TemporalAdjusters.firstDayOfMonth())
            val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth())
            return Pair(
                start.atStartOfDay(zone).toInstant().toEpochMilli(),
                end.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            )
        }

        // "last year"
        if (lower == "last year") {
            val lastYear = today.minusYears(1)
            val start = lastYear.with(TemporalAdjusters.firstDayOfYear())
            val end = lastYear.with(TemporalAdjusters.lastDayOfYear())
            return Pair(
                start.atStartOfDay(zone).toInstant().toEpochMilli(),
                end.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            )
        }

        // ISO format: 2025-07-12
        val isoMatch = Regex("(\\d{4})-(\\d{2})-(\\d{2})").find(lower)
        if (isoMatch != null) {
            val date = LocalDate.parse(isoMatch.value, DateTimeFormatter.ISO_LOCAL_DATE)
            return dayRange(date, zone)
        }

        // Slash format: 7/12/2025 or 07/12/2025
        val slashMatch = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{2,4})").find(lower)
        if (slashMatch != null) {
            try {
                val month = slashMatch.groupValues[1].toInt()
                val day = slashMatch.groupValues[2].toInt()
                var year = slashMatch.groupValues[3].toInt()
                if (year < 100) year += 2000
                val date = LocalDate.of(year, month, day)
                return dayRange(date, zone)
            } catch (_: Exception) { /* invalid date like Feb 30 */ }
        }

        // Month + optional day + optional year
        for ((name, month) in monthNames) {
            if (lower.contains(name)) {
                try {
                    val monthDayYear = Regex("$name\\s+(\\d{1,2}),?\\s*(\\d{4})").find(lower)
                    if (monthDayYear != null) {
                        val day = monthDayYear.groupValues[1].toInt()
                        val year = monthDayYear.groupValues[2].toInt()
                        val date = LocalDate.of(year, month, day)
                        return dayRange(date, zone)
                    }

                    val monthDay = Regex("$name\\s+(\\d{1,2})").find(lower)
                    if (monthDay != null) {
                        val day = monthDay.groupValues[1].toInt()
                        val date = LocalDate.of(today.year, month, day)
                        return dayRange(date, zone)
                    }
                } catch (_: Exception) { /* invalid date */ }

                // "may" without a day number should not be treated as a month
                if (name == "may") continue

                // Just month name (not "may")
                val start = LocalDate.of(today.year, month, 1)
                val end = start.with(TemporalAdjusters.lastDayOfMonth())
                return Pair(
                    start.atStartOfDay(zone).toInstant().toEpochMilli(),
                    end.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                )
            }
        }

        return null
    }

    private fun dayRange(date: LocalDate, zone: ZoneId): Pair<Long, Long> {
        return Pair(
            date.atStartOfDay(zone).toInstant().toEpochMilli(),
            date.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
        )
    }
}
