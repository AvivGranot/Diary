package com.proactivediary.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
import kotlinx.coroutines.delay

@Composable
fun StreakCelebration(
    streakCount: Int,
    onDismiss: () -> Unit,
    onShare: ((Int) -> Unit)? = null
) {
    val milestone = when {
        streakCount == 7 -> "One week"
        streakCount == 14 -> "Two weeks"
        streakCount == 30 -> "One month"
        streakCount == 50 -> "Fifty days"
        streakCount == 100 -> "One hundred days"
        streakCount == 365 -> "One year"
        else -> "Day $streakCount"
    }

    val message = when {
        streakCount == 7 -> "A week of writing.\nYour practice is taking root."
        streakCount == 14 -> "Two weeks in.\nYou\u2019re building something."
        streakCount == 30 -> "A month of writing.\nThis is who you are now."
        streakCount == 50 -> "Fifty days.\nMost people never get here."
        streakCount == 100 -> "One hundred days of practice.\nYour words are a treasure."
        streakCount == 365 -> "A full year of writing.\nExtraordinary."
        else -> "Keep writing."
    }

    var alpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        // Fade in
        val steps = 20
        for (i in 1..steps) {
            alpha = i.toFloat() / steps
            delay(20)
        }
        // Hold for 3 seconds
        delay(3000)
        // Fade out
        for (i in steps downTo 0) {
            alpha = i.toFloat() / steps
            delay(20)
        }
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color(0xFFF3EEE7))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Text(
                text = "$streakCount.",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 56.sp,
                    color = Color(0xFF313131)
                )
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = milestone,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 32.sp,
                    color = Color(0xFF313131)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF585858)
                ),
                textAlign = TextAlign.Center
            )

            if (onShare != null) {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF313131),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onShare(streakCount) }
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SHARE",
                        style = TextStyle(
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFF3EEE7)
                        )
                    )
                }
            }
        }
    }
}

fun isMilestone(streak: Int): Boolean {
    return streak in listOf(7, 14, 30, 50, 100, 365)
}
