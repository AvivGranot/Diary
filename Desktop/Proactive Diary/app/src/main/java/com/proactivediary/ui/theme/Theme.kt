package com.proactivediary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background = DiaryColors.Paper,
    onBackground = DiaryColors.Ink,
    surface = DiaryColors.Parchment,
    onSurface = DiaryColors.Ink,
    surfaceVariant = DiaryColors.Parchment,
    onSurfaceVariant = DiaryColors.Pencil,
    secondary = DiaryColors.Pencil,
    onSecondary = Color.White,
    primary = DiaryColors.Ink,
    onPrimary = DiaryColors.Parchment,
    outline = DiaryColors.Divider,
    outlineVariant = DiaryColors.Divider,
    surfaceContainerHighest = DiaryColors.Parchment,
)

@Composable
fun ProactiveDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DiaryTypography,
        shapes = DiaryShapes,
        content = content
    )
}
