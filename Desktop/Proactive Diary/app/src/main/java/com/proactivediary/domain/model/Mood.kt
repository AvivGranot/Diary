package com.proactivediary.domain.model

import androidx.compose.ui.graphics.Color

enum class Mood(val label: String, val key: String, val color: Color) {
    GREAT("Great", "great", Color(0xFF5B8C5A)),
    GOOD("Good", "good", Color(0xFF8FAD88)),
    NEUTRAL("Neutral", "neutral", Color(0xFFA0A09E)),
    BAD("Bad", "bad", Color(0xFF8B7B8B)),
    TERRIBLE("Terrible", "terrible", Color(0xFF8B5A5A));

    companion object {
        fun fromString(value: String?): Mood? {
            if (value == null) return null
            return entries.find { it.key.equals(value, ignoreCase = true) }
        }
    }
}
