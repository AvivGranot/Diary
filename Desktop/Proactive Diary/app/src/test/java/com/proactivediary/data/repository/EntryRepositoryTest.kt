package com.proactivediary.data.repository

import org.junit.Assert.*
import org.junit.Test

class EntryRepositoryTest {

    // ─── Word count logic (mirrors WriteViewModel.computeWordCount) ───

    private fun computeWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    }

    @Test
    fun `word count of empty string is 0`() {
        assertEquals(0, computeWordCount(""))
    }

    @Test
    fun `word count of blank string is 0`() {
        assertEquals(0, computeWordCount("   "))
    }

    @Test
    fun `word count of single word`() {
        assertEquals(1, computeWordCount("hello"))
    }

    @Test
    fun `word count with multiple spaces between words`() {
        assertEquals(3, computeWordCount("hello   world   today"))
    }

    @Test
    fun `word count with leading and trailing whitespace`() {
        assertEquals(2, computeWordCount("  hello world  "))
    }

    @Test
    fun `word count with newlines and tabs`() {
        assertEquals(4, computeWordCount("hello\nworld\there\n\ntest"))
    }

    @Test
    fun `word count of typical diary entry`() {
        val entry = "Today I went for a walk in the park. It was beautiful."
        assertEquals(12, computeWordCount(entry))
    }
}
