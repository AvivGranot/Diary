package com.proactivediary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = DarkPalette.background,
    onBackground = DarkPalette.onBackground,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.onSurface,
    surfaceVariant = DarkPalette.surfaceVariant,
    onSurfaceVariant = DarkPalette.onSurfaceVariant,
    primary = DarkPalette.primary,
    onPrimary = DarkPalette.onPrimary,
    primaryContainer = DarkPalette.primaryContainer,
    onPrimaryContainer = DarkPalette.onPrimaryContainer,
    secondary = DarkPalette.secondary,
    onSecondary = DarkPalette.onSecondary,
    secondaryContainer = DarkPalette.secondaryContainer,
    tertiary = DarkPalette.primary, // blue as tertiary too
    onTertiary = DarkPalette.onPrimary,
    outline = DarkPalette.outline,
    outlineVariant = DarkPalette.outlineVariant,
    surfaceContainerHighest = DarkPalette.surfaceContainerHighest,
    surfaceContainer = DarkPalette.surfaceContainer,
    surfaceContainerLow = DarkPalette.surfaceContainerLow,
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1A1A1A),
    inversePrimary = Color(0xFF2563EB),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
)

private val LightColorScheme = lightColorScheme(
    background = LightPalette.background,
    onBackground = LightPalette.onBackground,
    surface = LightPalette.surface,
    onSurface = LightPalette.onSurface,
    surfaceVariant = LightPalette.surfaceVariant,
    onSurfaceVariant = LightPalette.onSurfaceVariant,
    primary = LightPalette.primary,
    onPrimary = LightPalette.onPrimary,
    primaryContainer = LightPalette.primaryContainer,
    onPrimaryContainer = LightPalette.onPrimaryContainer,
    secondary = LightPalette.secondary,
    onSecondary = LightPalette.onSecondary,
    secondaryContainer = LightPalette.secondaryContainer,
    tertiary = LightPalette.primary,
    onTertiary = LightPalette.onPrimary,
    outline = LightPalette.outline,
    outlineVariant = LightPalette.outlineVariant,
    surfaceContainerHighest = LightPalette.surfaceContainerHighest,
    surfaceContainer = LightPalette.surfaceContainer,
    surfaceContainerLow = LightPalette.surfaceContainerLow,
    inverseSurface = Color(0xFF2A2A2A),
    inverseOnSurface = Color(0xFFF0F0F0),
    inversePrimary = Color(0xFF3B82F6),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
)

// Extended color sets for dark/light
private val DarkExtendedColors = DiaryExtendedColors(
    accent = Color(0xFF3B82F6),
    accentDark = Color(0xFF2563EB),
    accentContainer = Color(0xFF172554),
    cardSideLine = Color(0xFF3B82F6),
    divider = Color(0xFF333333),
    shimmerBase = Color(0xFF111111),
    shimmerHighlight = Color(0xFF1A1A1A),
    gradientStart = Color(0xFF3B82F6).copy(alpha = 0.15f),
    gradientEnd = Color(0xFF000000),
    bottomNavBackground = Color(0xFF111111),
    bottomNavIndicator = Color(0xFF3B82F6),
    streakGold = Color(0xFFFFD700),
    error = Color(0xFFEF4444),
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    morningGradient = listOf(Color(0xFF1A1500), Color(0xFF0D0A00)),
    afternoonGradient = listOf(Color(0xFF0A1020), Color(0xFF050810)),
    eveningGradient = listOf(Color(0xFF1A0A15), Color(0xFF0D050A)),
    nightGradient = listOf(Color(0xFF000000), Color(0xFF050510)),
)

private val LightExtendedColors = DiaryExtendedColors(
    accent = Color(0xFF2563EB),
    accentDark = Color(0xFF1D4ED8),
    accentContainer = Color(0xFFDBEAFE),
    cardSideLine = Color(0xFF2563EB),
    divider = Color(0xFFE0E0E0),
    shimmerBase = Color(0xFFF0F0F0),
    shimmerHighlight = Color(0xFFE0E0E0),
    gradientStart = Color(0xFF2563EB).copy(alpha = 0.1f),
    gradientEnd = Color(0xFFFAFAFA),
    bottomNavBackground = Color(0xFFFFFFFF),
    bottomNavIndicator = Color(0xFF2563EB),
    streakGold = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    success = Color(0xFF16A34A),
    warning = Color(0xFFF59E0B),
    morningGradient = listOf(Color(0xFFFFF7ED), Color(0xFFFEF3C7)),
    afternoonGradient = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
    eveningGradient = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3)),
    nightGradient = listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)),
)

@Composable
fun ProactiveDiaryTheme(
    darkTheme: Boolean = false, // V3.1: light mode by default
    accentColorKey: String = "blue",
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val baseExtendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Apply user-chosen accent color globally
    val accentOption = accentColorOptions.firstOrNull { it.key == accentColorKey }
    val extendedColors = if (accentOption != null && accentOption.key != "blue") {
        baseExtendedColors.copy(
            accent = accentOption.color,
            accentDark = accentOption.color,
            cardSideLine = accentOption.color,
            bottomNavIndicator = accentOption.color,
            success = accentOption.color,
            gradientStart = accentOption.color.copy(alpha = 0.15f)
        )
    } else {
        baseExtendedColors
    }

    // Set status bar color to match background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalDiaryExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DiaryTypography,
            shapes = DiaryShapes,
            content = content
        )
    }
}
