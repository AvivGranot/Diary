package com.proactivediary.domain.model

import androidx.compose.ui.graphics.Color
import com.proactivediary.ui.theme.DiaryColors

data class DiaryThemeConfig(
    val colorScheme: String = "cream",
    val primaryColor: Color = DiaryColors.Cream,
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

        fun colorForKey(key: String): Color {
            return when (key) {
                "cream" -> DiaryColors.Cream
                "blush" -> DiaryColors.Blush
                "sage" -> DiaryColors.Sage
                "sky" -> DiaryColors.Sky
                "lavender" -> DiaryColors.Lavender
                "midnight" -> DiaryColors.Midnight
                "charcoal" -> DiaryColors.Charcoal
                "wine_red" -> DiaryColors.WineRed
                "forest" -> DiaryColors.Forest
                "ocean" -> DiaryColors.Ocean
                else -> DiaryColors.Cream
            }
        }

        fun isDarkColor(key: String): Boolean {
            return key in listOf("midnight", "charcoal", "wine_red", "forest", "ocean")
        }

        fun textColorFor(key: String): Color {
            return if (isDarkColor(key)) Color(0xFFE8E7E5) else Color(0xFF313131)
        }

        fun secondaryTextColorFor(key: String): Color {
            return if (isDarkColor(key)) Color(0xFFA0A09E) else Color(0xFF585858)
        }
    }
}
