package com.proactivediary.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.components.ConfettiEffect
import androidx.compose.material3.MaterialTheme
import com.proactivediary.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay

@Composable
fun StreakCelebration(
    streakCount: Int,
    onDismiss: () -> Unit,
    onShare: ((Int) -> Unit)? = null
) {
    val milestone = when {
        streakCount == 7 -> "one week"
        streakCount == 14 -> "two weeks"
        streakCount == 21 -> "three weeks"
        streakCount == 30 -> "one month"
        streakCount == 50 -> "fifty days"
        streakCount == 100 -> "one hundred days"
        streakCount == 365 -> "one year"
        else -> "day $streakCount"
    }

    val message = when {
        streakCount == 7 -> "a week of writing.\nyour practice is taking root."
        streakCount == 14 -> "two weeks in.\nyou\u2019re building something real."
        streakCount == 21 -> "three weeks.\nit\u2019s becoming part of you."
        streakCount == 30 -> "a month of writing.\nthis is who you are now."
        streakCount == 50 -> "fifty days.\nmost people never get here."
        streakCount == 100 -> "one hundred days.\nyour words are a treasure."
        streakCount == 365 -> "a full year.\nextraordinary."
        else -> "keep writing."
    }

    var alpha by remember { mutableFloatStateOf(0f) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val steps = 20
        for (i in 1..steps) {
            alpha = i.toFloat() / steps
            delay(20)
        }
        showConfetti = true
        // Hold — don't auto-dismiss when share is available
        if (onShare == null) {
            delay(4000)
            for (i in steps downTo 0) {
                alpha = i.toFloat() / steps
                delay(20)
            }
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { if (onShare == null) onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Confetti overlay
        ConfettiEffect(
            trigger = showConfetti,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            // Fire emoji
            Text(
                text = "\uD83D\uDD25",
                fontSize = 48.sp
            )

            Spacer(Modifier.height(16.dp))

            // Streak count — big, bold
            Text(
                text = "$streakCount",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = milestone,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            if (onShare != null) {
                Spacer(Modifier.height(32.dp))

                // Share button — primary CTA (Nikita Bier: make sharing the default)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onShare(streakCount) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Share to Stories",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.3.sp
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Continue — secondary
                Text(
                    text = "continue writing",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() }
                        .padding(8.dp)
                )
            }
        }
    }
}

fun isMilestone(streak: Int): Boolean {
    return streak in listOf(7, 14, 21, 30, 50, 100, 365)
}

@Composable
fun FirstEntryCelebration(
    onDismiss: () -> Unit
) {
    var alpha by remember { mutableFloatStateOf(0f) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val steps = 20
        for (i in 1..steps) {
            alpha = i.toFloat() / steps
            delay(20)
        }
        showConfetti = true
        delay(4000)
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
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        ConfettiEffect(
            trigger = showConfetti,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Text(
                text = "\u2728",
                fontSize = 48.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "day one",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "your writing practice has begun",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.75f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
