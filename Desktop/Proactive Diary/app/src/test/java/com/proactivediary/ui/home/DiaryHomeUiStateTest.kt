package com.proactivediary.ui.home

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for DiaryHomeUiState, including the new streak message field (P1).
 */
class DiaryHomeUiStateTest {

    // ─── Default state ───

    @Test
    fun `default state has null streak message`() {
        val state = DiaryHomeUiState()
        assertNull(state.streakMessage)
    }

    @Test
    fun `default state is loading`() {
        val state = DiaryHomeUiState()
        assertTrue(state.isLoading)
    }

    @Test
    fun `default state is not empty`() {
        val state = DiaryHomeUiState()
        assertFalse(state.isEmpty)
    }

    @Test
    fun `default state has empty grouped entries`() {
        val state = DiaryHomeUiState()
        assertTrue(state.groupedEntries.isEmpty())
    }

    @Test
    fun `default state has non-empty today prompt`() {
        val state = DiaryHomeUiState()
        assertTrue(state.todayPrompt.isNotBlank())
    }

    // ─── Streak message ───

    @Test
    fun `streak message can be set`() {
        val state = DiaryHomeUiState(streakMessage = "5 days in a row. Keep going.")
        assertEquals("5 days in a row. Keep going.", state.streakMessage)
    }

    @Test
    fun `streak message can be empty string`() {
        val state = DiaryHomeUiState(streakMessage = "")
        assertEquals("", state.streakMessage)
    }

    @Test
    fun `copy preserves streak message`() {
        val original = DiaryHomeUiState(streakMessage = "Your first entry.")
        val copy = original.copy(isLoading = false)
        assertEquals("Your first entry.", copy.streakMessage)
    }

    @Test
    fun `copy can update streak message`() {
        val original = DiaryHomeUiState(streakMessage = "Old message")
        val updated = original.copy(streakMessage = "7 day streak. You're doing this.")
        assertEquals("7 day streak. You're doing this.", updated.streakMessage)
    }

    @Test
    fun `copy can clear streak message to null`() {
        val original = DiaryHomeUiState(streakMessage = "Some streak")
        val cleared = original.copy(streakMessage = null)
        assertNull(cleared.streakMessage)
    }

    // ─── State transitions ───

    @Test
    fun `loading to loaded transition`() {
        val loading = DiaryHomeUiState(isLoading = true)
        val loaded = loading.copy(isLoading = false, isEmpty = false)
        assertFalse(loaded.isLoading)
        assertFalse(loaded.isEmpty)
    }

    @Test
    fun `loading to empty transition`() {
        val loading = DiaryHomeUiState(isLoading = true)
        val empty = loading.copy(isLoading = false, isEmpty = true)
        assertFalse(empty.isLoading)
        assertTrue(empty.isEmpty)
    }

    @Test
    fun `streak message persists through loading state change`() {
        val withStreak = DiaryHomeUiState(
            streakMessage = "3 days in a row. Keep going.",
            isLoading = true
        )
        val loaded = withStreak.copy(isLoading = false)
        assertEquals("3 days in a row. Keep going.", loaded.streakMessage)
    }

    // ─── Equality ───

    @Test
    fun `two default states are equal`() {
        val s1 = DiaryHomeUiState()
        val s2 = DiaryHomeUiState()
        // todayPrompt is based on day-of-year, so same-day calls produce equal states
        assertEquals(s1.isLoading, s2.isLoading)
        assertEquals(s1.isEmpty, s2.isEmpty)
        assertEquals(s1.streakMessage, s2.streakMessage)
    }

    @Test
    fun `states with different streak messages are not equal`() {
        val s1 = DiaryHomeUiState(streakMessage = "Day 1")
        val s2 = DiaryHomeUiState(streakMessage = "Day 2")
        assertNotEquals(s1, s2)
    }
}
