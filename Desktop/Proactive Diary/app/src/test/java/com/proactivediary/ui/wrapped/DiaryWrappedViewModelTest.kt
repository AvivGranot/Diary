package com.proactivediary.ui.wrapped

import org.junit.Assert.*
import org.junit.Test

class DiaryWrappedViewModelTest {

    // ─── WrappedCard data class tests ───

    @Test
    fun `default WrappedCard has cream accent`() {
        val card = WrappedCard(type = WrappedCardType.INTRO, headline = "Test")
        assertEquals(0xFFF3EEE7, card.accentColorHex)
        assertEquals("", card.subtitle)
        assertEquals("", card.bigNumber)
        assertEquals("", card.detail)
    }

    @Test
    fun `WrappedCard preserves all fields`() {
        val card = WrappedCard(
            type = WrappedCardType.TOTAL_ENTRIES,
            headline = "You showed up",
            subtitle = "times",
            bigNumber = "42",
            detail = "Some narrative",
            accentColorHex = 0xFF4A6741
        )
        assertEquals(WrappedCardType.TOTAL_ENTRIES, card.type)
        assertEquals("You showed up", card.headline)
        assertEquals("times", card.subtitle)
        assertEquals("42", card.bigNumber)
        assertEquals("Some narrative", card.detail)
        assertEquals(0xFF4A6741, card.accentColorHex)
    }

    // ─── WrappedCardType enum tests ───

    @Test
    fun `WrappedCardType has 10 types`() {
        assertEquals(10, WrappedCardType.entries.size)
    }

    @Test
    fun `WrappedCardType contains all expected types`() {
        val types = WrappedCardType.entries.map { it.name }
        assertTrue(types.contains("INTRO"))
        assertTrue(types.contains("TOTAL_ENTRIES"))
        assertTrue(types.contains("TOTAL_WORDS"))
        assertTrue(types.contains("LONGEST_STREAK"))
        assertTrue(types.contains("FAVORITE_TIME"))
        assertTrue(types.contains("BUSIEST_DAY"))
        assertTrue(types.contains("TOP_LOCATION"))
        assertTrue(types.contains("LONGEST_ENTRY"))
        assertTrue(types.contains("FIRST_ENTRY"))
        assertTrue(types.contains("CLOSING"))
    }

    // ─── DiaryWrappedUiState tests ───

    @Test
    fun `default DiaryWrappedUiState is loading with empty cards`() {
        val state = DiaryWrappedUiState()
        assertTrue(state.isLoading)
        assertTrue(state.cards.isEmpty())
        assertEquals("", state.periodLabel)
        assertEquals("", state.userName)
    }

    @Test
    fun `DiaryWrappedUiState copy works correctly`() {
        val state = DiaryWrappedUiState(
            cards = listOf(WrappedCard(type = WrappedCardType.INTRO, headline = "Hello")),
            isLoading = false,
            periodLabel = "Jan - Feb 2026",
            userName = "Aviv"
        )
        assertEquals(1, state.cards.size)
        assertFalse(state.isLoading)
        assertEquals("Jan - Feb 2026", state.periodLabel)
        assertEquals("Aviv", state.userName)
    }
}
