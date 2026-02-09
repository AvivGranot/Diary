package com.proactivediary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.proactivediary.analytics.Experiment
import com.proactivediary.analytics.ExperimentService
import com.proactivediary.data.db.dao.PreferenceDao

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

private val DarkColorScheme = darkColorScheme(
    background = DiaryColors.PaperDark,
    onBackground = DiaryColors.InkDark,
    surface = DiaryColors.ParchmentDark,
    onSurface = DiaryColors.InkDark,
    surfaceVariant = DiaryColors.ParchmentDark,
    onSurfaceVariant = DiaryColors.PencilDark,
    secondary = DiaryColors.PencilDark,
    onSecondary = DiaryColors.PaperDark,
    primary = DiaryColors.InkDark,
    onPrimary = DiaryColors.ParchmentDark,
    outline = DiaryColors.DividerDark,
    outlineVariant = DiaryColors.DividerDark,
    surfaceContainerHighest = DiaryColors.ParchmentDark,
)

val LocalDarkMode = compositionLocalOf { false }

@Composable
fun ProactiveDiaryTheme(
    preferenceDao: PreferenceDao? = null,
    experimentService: ExperimentService? = null,
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = if (preferenceDao != null) {
        val darkModePref by preferenceDao.observe("dark_mode")
            .collectAsState(initial = null)
        when {
            // User has explicitly set a preference — always respect it
            darkModePref?.value != null -> darkModePref?.value == "true"
            // Experiment 6: Dark Mode Default — "system" variant follows system setting
            experimentService != null &&
                experimentService.isVariant(Experiment.DARK_MODE_DEFAULT, "system") -> darkMode
            // Control: always light
            else -> false
        }
    } else {
        darkMode
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalDarkMode provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DiaryTypography,
            shapes = DiaryShapes,
            content = content
        )
    }
}
