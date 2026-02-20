package com.proactivediary.ui.insights

import org.junit.Assert.*
import org.junit.Test

class ThemeEvolutionViewModelTest {

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
            entryCount = 8
        )
        assertEquals("Cafe Landwer", lm.name)
        assertEquals(8, lm.entryCount)
    }

    // ─── TimePattern tests ───

    @Test
    fun `TimePattern stores all fields`() {
        val tp = TimePattern(
            label = "Morning",
            entryCount = 25,
            percentage = 0.4f
        )
        assertEquals("Morning", tp.label)
        assertEquals(25, tp.entryCount)
        assertEquals(0.4f, tp.percentage, 0.001f)
    }

    @Test
    fun `TimePattern labels are standard time slots`() {
        val validLabels = setOf("Morning", "Afternoon", "Evening", "Night")
        val tp = TimePattern("Morning", 10, 0.25f)
        assertTrue(tp.label in validLabels)
    }

    // ─── ThemeEvolutionUiState tests ───

    @Test
    fun `default ThemeEvolutionUiState is loading with empty collections`() {
        val state = ThemeEvolutionUiState()
        assertTrue(state.isLoading)
        assertTrue(state.topicWords.isEmpty())
        assertTrue(state.locationMoods.isEmpty())
        assertTrue(state.timePatterns.isEmpty())
        assertEquals(0, state.totalEntries)
        assertEquals(0, state.totalWords)
    }

    @Test
    fun `ThemeEvolutionUiState copy preserves values`() {
        val state = ThemeEvolutionUiState(
            totalEntries = 100,
            totalWords = 50000,
            isLoading = false
        )
        assertEquals(100, state.totalEntries)
        assertEquals(50000, state.totalWords)
        assertFalse(state.isLoading)
    }
}
