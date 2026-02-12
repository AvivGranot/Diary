package com.proactivediary.domain.richtext

import android.os.Build
import android.text.Html

/**
 * Utilities for converting between HTML rich text and plain text.
 * Used for FTS indexing, word count, journal card previews, and export.
 */
object RichTextUtils {

    /**
     * Strip HTML tags to produce plain text.
     * Uses Android's Html.fromHtml for robust tag handling.
     */
    fun htmlToPlainText(html: String): String {
        if (html.isBlank()) return ""
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html).toString().trim()
        }
    }

    /**
     * Compute word count from text content.
     * Works with both plain text and HTML (strips tags first if HTML).
     */
    fun computeWordCount(text: String, isHtml: Boolean = false): Int {
        val plain = if (isHtml) htmlToPlainText(text) else text
        if (plain.isBlank()) return 0
        return plain.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    }

    /**
     * Get the effective plain text content from an entry's fields.
     * Prefers contentPlain, falls back to content.
     */
    fun getPlainContent(contentPlain: String?, content: String): String {
        return contentPlain ?: content
    }

    /**
     * Get the effective HTML content from an entry's fields.
     * Returns null if no HTML content exists (plain-text entry).
     */
    fun getDisplayHtml(contentHtml: String?): String? {
        return contentHtml?.takeIf { it.isNotBlank() }
    }
}
