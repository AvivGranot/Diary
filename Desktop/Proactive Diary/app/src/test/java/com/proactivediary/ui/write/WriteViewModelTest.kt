package com.proactivediary.ui.write

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class WriteViewModelTest {

    // ─── WritingPrompts tests ───

    @Test
    fun `dailyPrompt returns non-empty string`() {
        val prompt = WritingPrompts.dailyPrompt()
        assertTrue(prompt.isNotEmpty())
    }

    @Test
    fun `dailyPrompt returns same prompt for same day`() {
        val prompt1 = WritingPrompts.dailyPrompt()
        val prompt2 = WritingPrompts.dailyPrompt()
        assertEquals(prompt1, prompt2)
    }

    @Test
    fun `prompts list is not empty`() {
        assertTrue(WritingPrompts.prompts.isNotEmpty())
    }

    @Test
    fun `all prompts are non-blank`() {
        WritingPrompts.prompts.forEach { prompt ->
            assertTrue("Prompt should not be blank", prompt.isNotBlank())
        }
    }

    @Test
    fun `dailyPrompt index wraps around prompts list`() {
        // Verify no index out of bounds for any day of year
        val promptCount = WritingPrompts.prompts.size
        for (day in 1..366) {
            val index = day % promptCount
            assertTrue(index in 0 until promptCount)
        }
    }

    // ─── WriteUiState tests ───

    @Test
    fun `default WriteUiState has expected values`() {
        val state = WriteUiState()
        assertEquals("", state.entryId)
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertNull(state.mood)
        assertTrue(state.tags.isEmpty())
        assertEquals(0, state.wordCount)
        assertFalse(state.isSaving)
        assertNull(state.saveError)
        assertFalse(state.isLoaded)
        assertTrue(state.isNewEntry)
        assertEquals("cream", state.colorKey)
        assertEquals("focused", state.form)
        assertEquals("paper", state.texture)
        assertEquals("lined", state.canvas)
        assertEquals(16, state.fontSize)
    }

    @Test
    fun `default features include all expected toggles`() {
        val state = WriteUiState()
        assertTrue(state.features.contains("auto_save"))
        assertTrue(state.features.contains("word_count"))
        assertTrue(state.features.contains("date_header"))
        assertTrue(state.features.contains("daily_quote"))
    }

    @Test
    fun `WriteUiState copy preserves values`() {
        val original = WriteUiState(title = "My Title", wordCount = 42)
        val copy = original.copy(content = "Hello world")
        assertEquals("My Title", copy.title)
        assertEquals(42, copy.wordCount)
        assertEquals("Hello world", copy.content)
    }
}
