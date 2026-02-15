package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
import kotlinx.coroutines.delay

/**
 * A brief, beautiful overlay that greets the user when arriving from a notification.
 * Shows the streak count and the writing prompt, then fades out after 2.5 seconds.
 * Tap anywhere to dismiss immediately.
 */
@Composable
fun WelcomeBackOverlay(
    streak: Int,
    prompt: String,
    onDismiss: () -> Unit
) {
    var showOverlay by remember { mutableStateOf(false) }
    var showStreak by remember { mutableStateOf(false) }
    var showPrompt by remember { mutableStateOf(false) }

    // Staggered entrance animation
    LaunchedEffect(Unit) {
        showOverlay = true
        delay(200)
        showStreak = true
        delay(400)
        showPrompt = true
        delay(2500)
        onDismiss()
    }

    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                // Streak or welcome text
                AnimatedVisibility(
                    visible = showStreak,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Text(
                        text = if (streak > 0) "Day $streak." else "Welcome back.",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                // The writing prompt
                AnimatedVisibility(
                    visible = showPrompt,
                    enter = fadeIn(animationSpec = tween(600))
                ) {
                    Text(
                        text = "\u201C$prompt\u201D",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Subtle hint
                AnimatedVisibility(
                    visible = showPrompt,
                    enter = fadeIn(animationSpec = tween(400))
                ) {
                    Text(
                        text = "tap anywhere to start writing",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}
