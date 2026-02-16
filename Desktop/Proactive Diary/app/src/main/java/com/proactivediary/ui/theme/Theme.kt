package com.proactivediary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFFAFAFF),
    onBackground = Color(0xFF1E293B),
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF64748B),
    secondary = Color(0xFF64748B),
    onSecondary = Color.White,
    primary = DiaryColors.WineRed,
    onPrimary = Color.White,
    tertiary = DiaryColors.NeonPink,
    onTertiary = Color.White,
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFF8FAFC),
)

private val NightColorScheme = darkColorScheme(
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    primary = DiaryColors.WineRed,
    onPrimary = Color.White,
    tertiary = DiaryColors.NeonPink,
    onTertiary = Color.White,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    surfaceContainerHighest = Color(0xFF1E293B),
)

@Composable
fun ProactiveDiaryTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (DynamicColors.isNightMode()) NightColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DiaryTypography,
        shapes = DiaryShapes,
        content = content
    )
}
