package com.proactivediary.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

/**
 * Time-of-day awareness for the Write canvas background.
 * Mood and streak functions have been removed.
 */
object DynamicColors {

    enum class TimePeriod { MORNING, AFTERNOON, EVENING, NIGHT }

    fun currentTimePeriod(): TimePeriod {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> TimePeriod.MORNING
            hour in 12..16 -> TimePeriod.AFTERNOON
            hour in 17..20 -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }

    /**
     * Returns subtle dark gradient colors for the Write canvas based on time of day.
     */
    fun backgroundGradient(timePeriod: TimePeriod = currentTimePeriod()): List<Color> {
        return TimeGradients[timePeriod.name.lowercase()] ?: TimeGradients["night"]!!
    }

    /**
     * @deprecated Use isSystemInDarkTheme() instead. Kept temporarily for migration.
     */
    @Deprecated("Use isSystemInDarkTheme() from Theme.kt", replaceWith = ReplaceWith("true"))
    fun isNightMode(): Boolean = true // Always dark mode during migration

    fun accentForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        // All time periods now return mint green accent
        return Color(0xFF4ADE80)
    }

    fun textColorForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return Color(0xFFE0E0E0) // Always light text on dark background
    }

    fun secondaryTextForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return Color(0xFF888888) // Always muted text on dark background
    }

    fun surfaceForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return Color(0xFF111111).copy(alpha = 0.6f)
    }

    @Composable
    fun rememberBackgroundBrush(): Brush {
        val gradient = remember { backgroundGradient() }
        return Brush.verticalGradient(gradient)
    }
}
