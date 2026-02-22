package com.proactivediary.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Write-canvas theme configuration.
 * These colors are for the writing surface only (paper/texture),
 * NOT the app-wide theme. App theme uses MaterialTheme.colorScheme.
 */
data class DiaryThemeConfig(
    val colorScheme: String = "sky",
    val primaryColor: Color = Color(0xFFDDE4EC),
    val colorMode: String = "adaptive",
    val form: String = "focused",
    val texture: String = "paper",
    val canvas: String = "lined",
    val features: List<String> = listOf("auto_save", "word_count", "date_header", "daily_quote"),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif"
) {
    companion object {
        val DEFAULT = DiaryThemeConfig()

        // Canvas background colors (for Write screen paper only)
        private val canvasColors = mapOf(
            // Dark variants
            "dark" to Color(0xFF111111),
            "midnight" to Color(0xFF2C2C34),
            "charcoal" to Color(0xFF30302E),
            "wine_red" to Color(0xFF8B3A3A),
            "forest" to Color(0xFF3A5A40),
            "ocean" to Color(0xFF2C5F7C),
            // Light variants (default for new users)
            "cream" to Color(0xFFF3EEE7),
            "blush" to Color(0xFFF0E0D6),
            "sage" to Color(0xFFE4E8DF),
            "sky" to Color(0xFFDDE4EC),
            "lavender" to Color(0xFFE4DEE8),
            // Vibrant mist variants (dark)
            "indigo" to Color(0xFF0F0F2E),
            "pink" to Color(0xFF2E0F1F),
            "teal" to Color(0xFF0F2E2A),
            "sunset" to Color(0xFF2E1A0F),
            "purple" to Color(0xFF1A0F2E)
        )

        // Light canvas keys — these need dark text
        private val lightCanvasKeys = setOf(
            "cream", "blush", "sage", "sky", "lavender"
        )

        private val accentColors = mapOf(
            "indigo" to Color(0xFF6366F1),
            "pink" to Color(0xFFEC4899),
            "teal" to Color(0xFF14B8A6),
            "sunset" to Color(0xFFF97316),
            "purple" to Color(0xFF8B5CF6),
            "mint" to Color(0xFF4ADE80),
            "blue" to Color(0xFF3B82F6)
        )

        fun colorForKey(key: String): Color {
            return canvasColors[key] ?: Color(0xFFDDE4EC) // sky default
        }

        fun accentForKey(key: String): Color {
            return accentColors[key] ?: Color(0xFF3B82F6) // blue default
        }

        fun textureColorForKey(textureKey: String, canvasKey: String = "sky"): Color {
            // Light canvas → subtle light texture lines
            if (canvasKey in lightCanvasKeys) {
                return when (textureKey) {
                    "paper" -> Color(0xFFCDD4DC)
                    "parchment" -> Color(0xFFD5CEBF)
                    "linen" -> Color(0xFFCFCDCB)
                    "smooth" -> Color(0xFFD0D7DF)
                    "dark" -> Color(0xFFBCC3CB) // legacy dark texture on light bg
                    else -> Color(0xFFCDD4DC)
                }
            }
            // Dark canvas → dark texture lines
            return when (textureKey) {
                "paper" -> Color(0xFF1A1A1A)
                "parchment" -> Color(0xFF1C1A15)
                "linen" -> Color(0xFF1A1918)
                "smooth" -> Color(0xFF151515)
                "dark" -> Color(0xFF111111)
                else -> Color(0xFF111111)
            }
        }

        fun isTextureColorDark(textureKey: String): Boolean {
            return true // texture lines themselves are always subtle
        }

        fun textureTextColor(textureKey: String, canvasKey: String = "sky"): Color {
            return if (canvasKey in lightCanvasKeys) {
                Color(0xFF2A2A2A) // dark text on light canvas
            } else {
                Color(0xFFE8E7E5) // light text on dark canvas
            }
        }

        fun textureSecondaryTextColor(textureKey: String, canvasKey: String = "sky"): Color {
            return if (canvasKey in lightCanvasKeys) {
                Color(0xFF6B7280) // muted dark text on light canvas
            } else {
                Color(0xFFA0A09E) // muted light text on dark canvas
            }
        }

        fun isDarkColor(key: String): Boolean {
            return key !in lightCanvasKeys
        }

        fun textColorFor(key: String): Color {
            return if (key in lightCanvasKeys) {
                Color(0xFF2A2A2A) // dark text on light canvas
            } else {
                Color(0xFFE8E7E5) // light text on dark canvas
            }
        }

        fun secondaryTextColorFor(key: String): Color {
            return if (key in lightCanvasKeys) {
                Color(0xFF6B7280) // muted dark text on light canvas
            } else {
                Color(0xFFA0A09E) // muted light text on dark canvas
            }
        }
    }
}
