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

    // Classic diary color options (for Design Studio)
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

    // Vibrant Gen Z palette
    val ElectricIndigo = Color(0xFF6366F1)
    val NeonPink = Color(0xFFEC4899)
    val CyberTeal = Color(0xFF14B8A6)
    val SunsetOrange = Color(0xFFF97316)
    val ElectricBlue = Color(0xFF3B82F6)
    val VividPurple = Color(0xFF8B5CF6)
    val LimeGreen = Color(0xFF84CC16)
    val CoralRed = Color(0xFFF43F5E)

    // Vibrant light backgrounds (tinted, not flat white)
    val IndigoMist = Color(0xFFF0F0FF)
    val PinkMist = Color(0xFFFDF2F8)
    val TealMist = Color(0xFFF0FDFA)
    val OrangeMist = Color(0xFFFFF7ED)
    val PurpleMist = Color(0xFFF5F3FF)
}

data class DiaryColorOption(
    val name: String,
    val key: String,
    val color: Color,
    val accentColor: Color? = null
)

// Mood gradient pairs
val MoodGradients = mapOf(
    "great" to listOf(DiaryColors.ElectricIndigo, DiaryColors.NeonPink),
    "good" to listOf(DiaryColors.CyberTeal, DiaryColors.ElectricBlue),
    "okay" to listOf(DiaryColors.SunsetOrange, Color(0xFFFBBF24)),
    "bad" to listOf(DiaryColors.VividPurple, DiaryColors.ElectricIndigo),
    "awful" to listOf(Color(0xFF64748B), Color(0xFF475569))
)

// Time-of-day background gradients
val TimeGradients = mapOf(
    "morning" to listOf(Color(0xFFFFF7ED), Color(0xFFFEF3C7)),
    "afternoon" to listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
    "evening" to listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3)),
    "night" to listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
)

val diaryColorOptions = listOf(
    DiaryColorOption("Indigo", "indigo", DiaryColors.IndigoMist, DiaryColors.ElectricIndigo),
    DiaryColorOption("Pink", "pink", DiaryColors.PinkMist, DiaryColors.NeonPink),
    DiaryColorOption("Teal", "teal", DiaryColors.TealMist, DiaryColors.CyberTeal),
    DiaryColorOption("Sunset", "sunset", DiaryColors.OrangeMist, DiaryColors.SunsetOrange),
    DiaryColorOption("Purple", "purple", DiaryColors.PurpleMist, DiaryColors.VividPurple),
)

// Classic options for users who prefer the old style
val classicColorOptions = listOf(
    DiaryColorOption("Cream", "cream", DiaryColors.Cream),
    DiaryColorOption("Blush", "blush", DiaryColors.Blush),
    DiaryColorOption("Sage", "sage", DiaryColors.Sage),
    DiaryColorOption("Sky", "sky", DiaryColors.Sky),
    DiaryColorOption("Lavender", "lavender", DiaryColors.Lavender),
)
