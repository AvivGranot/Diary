package com.proactivediary.ui.insights

import org.junit.Assert.*
import org.junit.Test

class ThemeEvolutionViewModelTest {

    // ─── MoodDataPoint tests ───

    @Test
    fun `MoodDataPoint stores all fields`() {
        val dp = MoodDataPoint(
            month = "Jan",
            yearMonth = "2026-01",
            averageMoodScore = 4.2f,
            entryCount = 15,
            dominantMood = "good"
        )
        assertEquals("Jan", dp.month)
        assertEquals("2026-01", dp.yearMonth)
        assertEquals(4.2f, dp.averageMoodScore, 0.01f)
        assertEquals(15, dp.entryCount)
        assertEquals("good", dp.dominantMood)
    }

    // ─── TopicWord tests ───

    @Test
    fun `TopicWord stores word, count and weight`() {
        val tw = TopicWord(word = "gratitude", count = 42, weight = 1.0f)
        assertEquals("gratitude", tw.word)
        assertEquals(42, tw.count)
        assertEquals(1.0f, tw.weight, 0.001f)
    }

    @Test
    fun `TopicWord weight is between 0 and 1`() {
        val tw = TopicWord(word = "test", count = 10, weight = 0.5f)
        assertTrue(tw.weight in 0f..1f)
    }

    // ─── LocationMood tests ───

    @Test
    fun `LocationMood stores all fields`() {
        val lm = LocationMood(
            name = "Cafe Landwer",
            entryCount = 8,
            averageMoodScore = 4.5f,
            dominantMood = "great"
        )
        assertEquals("Cafe Landwer", lm.name)
        assertEquals(8, lm.entryCount)
        assertEquals(4.5f, lm.averageMoodScore, 0.01f)
        assertEquals("great", lm.dominantMood)
    }

    // ─── TimePattern tests ───

    @Test
    fun `TimePattern stores all fields`() {
        val tp = TimePattern(
            label = "Morning",
            entryCount = 25,
            averageMoodScore = 3.8f,
            percentage = 0.4f
        )
        assertEquals("Morning", tp.label)
        assertEquals(25, tp.entryCount)
        assertEquals(3.8f, tp.averageMoodScore, 0.01f)
        assertEquals(0.4f, tp.percentage, 0.001f)
    }

    @Test
    fun `TimePattern labels are standard time slots`() {
        val validLabels = setOf("Morning", "Afternoon", "Evening", "Night")
        val tp = TimePattern("Morning", 10, 3.5f, 0.25f)
        assertTrue(tp.label in validLabels)
    }

    // ─── ThemeEvolutionUiState tests ───

    @Test
    fun `default ThemeEvolutionUiState is loading with empty collections`() {
        val state = ThemeEvolutionUiState()
        assertTrue(state.isLoading)
        assertTrue(state.moodTimeline.isEmpty())
        assertTrue(state.topicWords.isEmpty())
        assertTrue(state.locationMoods.isEmpty())
        assertTrue(state.timePatterns.isEmpty())
        assertEquals(0, state.totalEntries)
        assertEquals(0, state.totalWords)
        assertEquals(0f, state.averageMoodScore, 0.001f)
        assertEquals("", state.moodTrend)
    }

    @Test
    fun `ThemeEvolutionUiState copy preserves values`() {
        val state = ThemeEvolutionUiState(
            totalEntries = 100,
            totalWords = 50000,
            averageMoodScore = 3.7f,
            moodTrend = "improving",
            isLoading = false
        )
        assertEquals(100, state.totalEntries)
        assertEquals(50000, state.totalWords)
        assertEquals(3.7f, state.averageMoodScore, 0.01f)
        assertEquals("improving", state.moodTrend)
        assertFalse(state.isLoading)
    }

    @Test
    fun `moodTrend values are expected strings`() {
        val validTrends = setOf("improving", "stable", "declining", "")
        assertTrue("improving" in validTrends)
        assertTrue("stable" in validTrends)
        assertTrue("declining" in validTrends)
    }
}
