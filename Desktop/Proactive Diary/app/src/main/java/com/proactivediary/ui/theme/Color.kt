package com.proactivediary.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dark palette primitives ──────────────────────────────────────────
private val Black = Color(0xFF000000)
private val Surface111 = Color(0xFF1A1A1A)
private val SurfaceVariant1A = Color(0xFF242424)
private val SurfaceElevated22 = Color(0xFF2A2A2A)
private val White = Color(0xFFFFFFFF)
private val OnSurfaceE0 = Color(0xFFE0E0E0)
private val OnSurfaceVariant88 = Color(0xFFB0B0B0)
private val Outline33 = Color(0xFF333333)
private val MintGreen = Color(0xFF4ADE80) // kept for accent picker option only
private val MintGreenDark = Color(0xFF22C55E)
private val MintContainer = Color(0xFF1A2E1A)

// ── Blue palette primitives (new default accent) ─────────────────────
private val Blue = Color(0xFF3B82F6)
private val BlueDark = Color(0xFF2563EB)
private val BlueContainer = Color(0xFF172554)

// ── Light palette primitives ─────────────────────────────────────────
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF5F5F5)
private val LightOnBackground = Color(0xFF1A1A1A)
private val LightOnSurface = Color(0xFF2A2A2A)
private val LightOnSurfaceVariant = Color(0xFF666666)
private val LightOutline = Color(0xFFE0E0E0)
private val LightMintGreen = Color(0xFF16A34A) // kept for accent picker option only
private val LightBlue = Color(0xFF2563EB)

// ── Semantic dark scheme colors (used in Theme.kt) ───────────────────
object DarkPalette {
    val background = Black
    val surface = Surface111
    val surfaceVariant = SurfaceVariant1A
    val surfaceElevated = SurfaceElevated22
    val onBackground = White
    val onSurface = OnSurfaceE0
    val onSurfaceVariant = OnSurfaceVariant88
    val primary = Blue
    val onPrimary = White
    val primaryContainer = BlueContainer
    val onPrimaryContainer = Blue
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
    val primary = LightBlue
    val onPrimary = White
    val primaryContainer = Color(0xFFDBEAFE)
    val onPrimaryContainer = Color(0xFF1E3A5F)
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
    val accent: Color = Blue,
    val accentDark: Color = BlueDark,
    val accentContainer: Color = BlueContainer,
    val cardSideLine: Color = Blue,
    val divider: Color = Outline33,
    val shimmerBase: Color = Surface111,
    val shimmerHighlight: Color = SurfaceVariant1A,
    val gradientStart: Color = Blue.copy(alpha = 0.15f),
    val gradientEnd: Color = Black,
    val bottomNavBackground: Color = Surface111,
    val bottomNavIndicator: Color = Blue,
    val streakGold: Color = Color(0xFFFFD700),
    val error: Color = Color(0xFFEF4444),
    val success: Color = Color(0xFF4ADE80), // success stays green (semantic)
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

// All DiaryColors, diaryColorOptions, classicColorOptions, and MoodGradients
// shims have been removed — migration to MaterialTheme.colorScheme is complete.
