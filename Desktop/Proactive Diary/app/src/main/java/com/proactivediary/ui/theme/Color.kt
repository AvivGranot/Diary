package com.proactivediary.ui.theme

import androidx.compose.ui.graphics.Color

object DiaryColors {
    // Light mode
    val Paper = Color(0xFFF3EEE7)
    val Ink = Color(0xFF313131)
    val Pencil = Color(0xFF585858)
    val Parchment = Color(0xFFFAF9F5)
    val Divider = Color(0xFF585858).copy(alpha = 0.15f)
    val Shadow = Color(0xFF313131).copy(alpha = 0.08f)

    // Dark mode
    val PaperDark = Color(0xFF1A1918)
    val InkDark = Color(0xFFE8E7E5)
    val PencilDark = Color(0xFFA0A09E)
    val ParchmentDark = Color(0xFF2A2928)
    val DividerDark = Color(0xFFA0A09E).copy(alpha = 0.15f)
    val ShadowDark = Color(0xFFE8E7E5).copy(alpha = 0.08f)

    // Diary color options (for Design Studio)
    val Cream = Color(0xFFF3EEE7)
    val Blush = Color(0xFFF0E0D6)
    val Sage = Color(0xFFE4E8DF)
    val Sky = Color(0xFFDDE4EC)
    val Lavender = Color(0xFFE4DEE8)
    val Midnight = Color(0xFF2C2C34)
    val Charcoal = Color(0xFF30302E)
    val WineRed = Color(0xFF8B3A3A)
    val Forest = Color(0xFF3A5A40)
    val Ocean = Color(0xFF2C5F7C)
}

data class DiaryColorOption(
    val name: String,
    val key: String,
    val color: Color
)

val diaryColorOptions = listOf(
    DiaryColorOption("Cream", "cream", DiaryColors.Cream),
    DiaryColorOption("Blush", "blush", DiaryColors.Blush),
    DiaryColorOption("Sage", "sage", DiaryColors.Sage),
    DiaryColorOption("Sky", "sky", DiaryColors.Sky),
    DiaryColorOption("Lavender", "lavender", DiaryColors.Lavender),
    DiaryColorOption("Wine Red", "wine_red", DiaryColors.WineRed),
    DiaryColorOption("Forest", "forest", DiaryColors.Forest),
    DiaryColorOption("Ocean", "ocean", DiaryColors.Ocean),
)
