package com.proactivediary.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dark palette primitives ──────────────────────────────────────────
private val Black = Color(0xFF000000)
private val Surface111 = Color(0xFF111111)
private val SurfaceVariant1A = Color(0xFF1A1A1A)
private val SurfaceElevated22 = Color(0xFF222222)
private val White = Color(0xFFFFFFFF)
private val OnSurfaceE0 = Color(0xFFE0E0E0)
private val OnSurfaceVariant88 = Color(0xFF888888)
private val Outline33 = Color(0xFF333333)
private val MintGreen = Color(0xFF4ADE80)
private val MintGreenDark = Color(0xFF22C55E)
private val MintContainer = Color(0xFF1A2E1A)

// ── Light palette primitives ─────────────────────────────────────────
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF5F5F5)
private val LightOnBackground = Color(0xFF1A1A1A)
private val LightOnSurface = Color(0xFF2A2A2A)
private val LightOnSurfaceVariant = Color(0xFF666666)
private val LightOutline = Color(0xFFE0E0E0)
private val LightMintGreen = Color(0xFF16A34A)

// ── Semantic dark scheme colors (used in Theme.kt) ───────────────────
object DarkPalette {
    val background = Black
    val surface = Surface111
    val surfaceVariant = SurfaceVariant1A
    val surfaceElevated = SurfaceElevated22
    val onBackground = White
    val onSurface = OnSurfaceE0
    val onSurfaceVariant = OnSurfaceVariant88
    val primary = MintGreen
    val onPrimary = Black
    val primaryContainer = MintContainer
    val onPrimaryContainer = MintGreen
    val secondary = OnSurfaceVariant88
    val onSecondary = Black
    val secondaryContainer = SurfaceVariant1A
    val outline = Outline33
    val outlineVariant = Color(0xFF2A2A2A)
    val surfaceContainerHighest = SurfaceElevated22
    val surfaceContainer = Surface111
    val surfaceContainerLow = Color(0xFF0A0A0A)
}

object LightPalette {
    val background = LightBackground
    val surface = LightSurface
    val surfaceVariant = LightSurfaceVariant
    val surfaceElevated = Color(0xFFFFFFFF)
    val onBackground = LightOnBackground
    val onSurface = LightOnSurface
    val onSurfaceVariant = LightOnSurfaceVariant
    val primary = LightMintGreen
    val onPrimary = White
    val primaryContainer = Color(0xFFDCFCE7)
    val onPrimaryContainer = Color(0xFF14532D)
    val secondary = LightOnSurfaceVariant
    val onSecondary = White
    val secondaryContainer = LightSurfaceVariant
    val outline = LightOutline
    val outlineVariant = Color(0xFFEEEEEE)
    val surfaceContainerHighest = Color(0xFFF0F0F0)
    val surfaceContainer = LightSurface
    val surfaceContainerLow = Color(0xFFF8F8F8)
}

// ── Extended colors (provided via CompositionLocal) ──────────────────
@Immutable
data class DiaryExtendedColors(
    val accent: Color = MintGreen,
    val accentDark: Color = MintGreenDark,
    val accentContainer: Color = MintContainer,
    val cardSideLine: Color = MintGreen,
    val divider: Color = Outline33,
    val shimmerBase: Color = Surface111,
    val shimmerHighlight: Color = SurfaceVariant1A,
    val gradientStart: Color = MintGreen.copy(alpha = 0.15f),
    val gradientEnd: Color = Black,
    val bottomNavBackground: Color = Surface111,
    val bottomNavIndicator: Color = MintGreen,
    val streakGold: Color = Color(0xFFFFD700),
    val error: Color = Color(0xFFEF4444),
    val success: Color = MintGreen,
    val warning: Color = Color(0xFFFBBF24),
    // Time-of-day write canvas gradients (kept for Write screen only)
    val morningGradient: List<Color> = listOf(Color(0xFF1A1500), Color(0xFF0D0A00)),
    val afternoonGradient: List<Color> = listOf(Color(0xFF0A1020), Color(0xFF050810)),
    val eveningGradient: List<Color> = listOf(Color(0xFF1A0A15), Color(0xFF0D050A)),
    val nightGradient: List<Color> = listOf(Color(0xFF000000), Color(0xFF050510)),
)

val LocalDiaryExtendedColors = staticCompositionLocalOf { DiaryExtendedColors() }

// ── Accent color options (for Layout page accent picker) ─────────────
data class DiaryColorOption(
    val name: String,
    val key: String,
    val color: Color,
    val accentColor: Color? = null
)

val accentColorOptions = listOf(
    DiaryColorOption("Mint", "mint", MintGreen, MintGreen),
    DiaryColorOption("Indigo", "indigo", Color(0xFF6366F1), Color(0xFF6366F1)),
    DiaryColorOption("Pink", "pink", Color(0xFFEC4899), Color(0xFFEC4899)),
    DiaryColorOption("Teal", "teal", Color(0xFF14B8A6), Color(0xFF14B8A6)),
    DiaryColorOption("Sunset", "sunset", Color(0xFFF97316), Color(0xFFF97316)),
    DiaryColorOption("Purple", "purple", Color(0xFF8B5CF6), Color(0xFF8B5CF6)),
    DiaryColorOption("Blue", "blue", Color(0xFF3B82F6), Color(0xFF3B82F6)),
    DiaryColorOption("Coral", "coral", Color(0xFFF43F5E), Color(0xFFF43F5E)),
)

// ── Time-of-day gradients (Write canvas only) ────────────────────────
val TimeGradients = mapOf(
    "morning" to listOf(Color(0xFF1A1500), Color(0xFF0D0A00)),
    "afternoon" to listOf(Color(0xFF0A1020), Color(0xFF050810)),
    "evening" to listOf(Color(0xFF1A0A15), Color(0xFF0D050A)),
    "night" to listOf(Color(0xFF000000), Color(0xFF050510))
)

// ── @Deprecated shims (kept during migration, remove in Phase 6) ─────
@Deprecated("Use MaterialTheme.colorScheme or DarkPalette/LightPalette", replaceWith = ReplaceWith("DarkPalette"))
object DiaryColors {
    val Paper = LightBackground
    val Ink = LightOnBackground
    val Pencil = LightOnSurfaceVariant
    val Parchment = LightSurface
    val Divider = Outline33
    val Shadow = Black.copy(alpha = 0.08f)

    val PaperDark = Black
    val InkDark = White
    val PencilDark = OnSurfaceVariant88
    val ParchmentDark = Surface111
    val DividerDark = Outline33
    val ShadowDark = White.copy(alpha = 0.08f)

    // Keep individual colors for any remaining references
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

    val ElectricIndigo = Color(0xFF6366F1)
    val NeonPink = Color(0xFFEC4899)
    val CyberTeal = Color(0xFF14B8A6)
    val SunsetOrange = Color(0xFFF97316)
    val ElectricBlue = Color(0xFF3B82F6)
    val VividPurple = Color(0xFF8B5CF6)
    val LimeGreen = Color(0xFF84CC16)
    val CoralRed = Color(0xFFF43F5E)

    val IndigoMist = Color(0xFFF0F0FF)
    val PinkMist = Color(0xFFFDF2F8)
    val TealMist = Color(0xFFF0FDFA)
    val OrangeMist = Color(0xFFFFF7ED)
    val PurpleMist = Color(0xFFF5F3FF)
}

// ── @Deprecated shims for old color option lists ─────────────────────
@Deprecated("Use accentColorOptions", replaceWith = ReplaceWith("accentColorOptions"))
val diaryColorOptions = accentColorOptions

@Deprecated("Use accentColorOptions", replaceWith = ReplaceWith("accentColorOptions"))
val classicColorOptions = listOf(
    DiaryColorOption("Cream", "cream", Color(0xFFF3EEE7)),
    DiaryColorOption("Blush", "blush", Color(0xFFF0E0D6)),
    DiaryColorOption("Sage", "sage", Color(0xFFE4E8DF)),
    DiaryColorOption("Sky", "sky", Color(0xFFDDE4EC)),
    DiaryColorOption("Lavender", "lavender", Color(0xFFE4DEE8)),
)

// ── @Deprecated mood gradients shim (will be removed with mood code) ─
@Deprecated("Mood feature removed")
val MoodGradients = mapOf(
    "great" to listOf(MintGreen, MintGreen),
    "good" to listOf(MintGreen, MintGreen),
    "okay" to listOf(MintGreen, MintGreen),
    "bad" to listOf(MintGreen, MintGreen),
    "awful" to listOf(MintGreen, MintGreen)
)
