package com.proactivediary.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Calendar

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

    fun backgroundGradient(timePeriod: TimePeriod = currentTimePeriod()): List<Color> {
        return when (timePeriod) {
            TimePeriod.MORNING -> listOf(Color(0xFFFFF7ED), Color(0xFFFEF3C7))
            TimePeriod.AFTERNOON -> listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
            TimePeriod.EVENING -> listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3))
            TimePeriod.NIGHT -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
        }
    }

    fun isNightMode(): Boolean = currentTimePeriod() == TimePeriod.NIGHT

    fun accentForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return when (timePeriod) {
            TimePeriod.MORNING -> DiaryColors.SunsetOrange
            TimePeriod.AFTERNOON -> DiaryColors.ElectricBlue
            TimePeriod.EVENING -> DiaryColors.NeonPink
            TimePeriod.NIGHT -> DiaryColors.VividPurple
        }
    }

    fun textColorForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return if (timePeriod == TimePeriod.NIGHT) Color(0xFFE8E7E5) else Color(0xFF1E293B)
    }

    fun secondaryTextForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return if (timePeriod == TimePeriod.NIGHT) Color(0xFFA0A09E) else Color(0xFF64748B)
    }

    fun surfaceForTime(timePeriod: TimePeriod = currentTimePeriod()): Color {
        return if (timePeriod == TimePeriod.NIGHT) Color(0xFF1E1B4B).copy(alpha = 0.6f)
        else Color.White.copy(alpha = 0.7f)
    }

    fun moodGradient(moodKey: String): List<Color> {
        return MoodGradients[moodKey] ?: MoodGradients["okay"]!!
    }

    fun moodAccent(moodKey: String): Color {
        return when (moodKey) {
            "great" -> DiaryColors.ElectricIndigo
            "good" -> DiaryColors.CyberTeal
            "okay" -> DiaryColors.SunsetOrange
            "bad" -> DiaryColors.VividPurple
            "awful" -> Color(0xFF64748B)
            else -> DiaryColors.ElectricIndigo
        }
    }

    fun streakGlow(streakDays: Int): Color {
        return when {
            streakDays >= 30 -> DiaryColors.NeonPink.copy(alpha = 0.4f)
            streakDays >= 14 -> DiaryColors.ElectricIndigo.copy(alpha = 0.3f)
            streakDays >= 7 -> DiaryColors.CyberTeal.copy(alpha = 0.2f)
            else -> Color.Transparent
        }
    }

    @Composable
    fun rememberBackgroundBrush(): Brush {
        val gradient = remember { backgroundGradient() }
        return Brush.verticalGradient(gradient)
    }
}
