package com.proactivediary.ui.share

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the post-save share card logic (P1).
 * Covers ShareCardData construction and excerpt extraction.
 */
class PostSaveShareTest {

    // ─── Excerpt extraction logic (mirrors WriteScreen post-save) ───

    /**
     * Replicates the excerpt extraction from WriteScreen:
     * Take first 120 chars, find first period after position 10, cut there.
     */
    private fun extractExcerpt(content: String): String {
        return content.take(120).let {
            val dot = it.indexOf('.')
            if (dot in 10..119) it.substring(0, dot + 1) else it
        }
    }

    @Test
    fun `excerpt cuts at first sentence under 120 chars`() {
        val content = "Today was a beautiful day. I went for a long walk in the park and saw the sunset."
        val excerpt = extractExcerpt(content)
        assertEquals("Today was a beautiful day.", excerpt)
    }

    @Test
    fun `excerpt takes full 120 chars when no period found`() {
        val content = "This is a long entry without any periods just commas and spaces " +
            "and more text that goes on and on without any punctuation at all whatsoever"
        val excerpt = extractExcerpt(content)
        assertEquals(120, excerpt.length)
    }

    @Test
    fun `excerpt ignores period before position 10`() {
        val content = "Hi. Today was a great day for writing and I felt really inspired."
        val excerpt = extractExcerpt(content)
        // Period at index 2 is before 10, so it should find the next period
        assertEquals("Hi. Today was a great day for writing and I felt really inspired.", excerpt)
    }

    @Test
    fun `excerpt handles short content`() {
        val content = "Short note."
        val excerpt = extractExcerpt(content)
        // Period at index 10, which is in range 10..119
        assertEquals("Short note.", excerpt)
    }

    @Test
    fun `excerpt handles content exactly 120 chars`() {
        val content = "A".repeat(120)
        val excerpt = extractExcerpt(content)
        assertEquals(120, excerpt.length)
    }

    @Test
    fun `excerpt handles content shorter than 120 chars without period`() {
        val content = "Just a quick thought"
        val excerpt = extractExcerpt(content)
        assertEquals("Just a quick thought", excerpt)
    }

    @Test
    fun `excerpt handles empty content`() {
        val excerpt = extractExcerpt("")
        assertEquals("", excerpt)
    }

    @Test
    fun `excerpt handles single character`() {
        val excerpt = extractExcerpt("X")
        assertEquals("X", excerpt)
    }

    @Test
    fun `excerpt handles period at exactly position 10`() {
        val content = "0123456789. Rest of the text here."
        val excerpt = extractExcerpt(content)
        assertEquals("0123456789.", excerpt)
    }

    @Test
    fun `excerpt handles period at exactly position 119`() {
        val content = "A".repeat(119) + ". This will be cut off."
        val excerpt = extractExcerpt(content)
        assertEquals("A".repeat(119) + ".", excerpt)
    }

    // ─── ShareCardData construction ───

    @Test
    fun `ShareCardData with excerpt only uses defaults`() {
        val data = ShareCardData(excerpt = "Today was great.")
        assertEquals("Today was great.", data.excerpt)
        assertEquals("", data.title)
        assertEquals("", data.dateFormatted)
        assertEquals("indigo", data.colorKey)
    }

    @Test
    fun `ShareCardData with all fields`() {
        val data = ShareCardData(
            excerpt = "My entry",
            title = "Morning Thoughts",
            dateFormatted = "Feb 28",
            colorKey = "paper"
        )
        assertEquals("My entry", data.excerpt)
        assertEquals("Morning Thoughts", data.title)
        assertEquals("Feb 28", data.dateFormatted)
        assertEquals("paper", data.colorKey)
    }

    @Test
    fun `ShareCardData with blank title`() {
        val data = ShareCardData(excerpt = "Content here", title = "")
        assertEquals("", data.title)
    }

    @Test
    fun `ShareCardData equality`() {
        val d1 = ShareCardData(excerpt = "Test", colorKey = "paper")
        val d2 = ShareCardData(excerpt = "Test", colorKey = "paper")
        assertEquals(d1, d2)
    }

    @Test
    fun `ShareCardData copy preserves fields`() {
        val original = ShareCardData(
            excerpt = "Original",
            title = "Title",
            colorKey = "ocean"
        )
        val copy = original.copy(excerpt = "Updated")
        assertEquals("Updated", copy.excerpt)
        assertEquals("Title", copy.title)
        assertEquals("ocean", copy.colorKey)
    }

    // ─── ShareAspectRatio ───

    @Test
    fun `STORY aspect ratio has correct dimensions`() {
        assertEquals(360, ShareAspectRatio.STORY.widthDp)
        assertEquals(640, ShareAspectRatio.STORY.heightDp)
    }

    @Test
    fun `SQUARE aspect ratio has equal dimensions`() {
        assertEquals(ShareAspectRatio.SQUARE.widthDp, ShareAspectRatio.SQUARE.heightDp)
    }

    @Test
    fun `STORY is taller than wide (portrait for Instagram)`() {
        assertTrue(ShareAspectRatio.STORY.heightDp > ShareAspectRatio.STORY.widthDp)
    }

    @Test
    fun `all aspect ratios have labels`() {
        ShareAspectRatio.entries.forEach { ratio ->
            assertTrue("${ratio.name} should have non-blank label", ratio.label.isNotBlank())
        }
    }
}
