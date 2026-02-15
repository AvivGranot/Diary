package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Dictation button — pure speech-to-text. No audio file is saved.
 * The spoken word becomes written text, directly into the editor.
 *
 * When active: pulsing blue indicator + live waveform + elapsed time + stop.
 * When inactive: subtle mic icon with "Dictate" label.
 */
@Composable
fun VoiceRecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    var durationSeconds by remember { mutableStateOf(0) }
    var showDone by remember { mutableStateOf(false) }
    var wasRecording by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Track dictation state transitions
    LaunchedEffect(isRecording) {
        if (isRecording) {
            showDone = false
            durationSeconds = 0
            while (true) {
                delay(1000)
                durationSeconds++
            }
        } else {
            if (wasRecording) {
                showDone = true
                delay(2000)
                showDone = false
            }
            durationSeconds = 0
        }
    }
    LaunchedEffect(isRecording) { wasRecording = isRecording }

    // Breathing pulse for active state
    val infiniteTransition = rememberInfiniteTransition(label = "dictation")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Waveform bars — 5 bars at organic rhythms
    val wave1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "w1")
    val wave2 by infiniteTransition.animateFloat(0.5f, 0.8f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "w2")
    val wave3 by infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "w3")
    val wave4 by infiniteTransition.animateFloat(0.6f, 0.9f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "w4")
    val wave5 by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(280), RepeatMode.Reverse), label = "w5")

    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) pulseScale else 1f,
        label = "btnScale"
    )

    val activeColor = Color(0xFF1565C0) // DeepMind blue — intelligence, not danger

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .scale(buttonScale)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isRecording) activeColor.copy(alpha = 0.10f)
                    else secondaryTextColor.copy(alpha = 0.08f)
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRecording) onStopRecording() else onStartRecording()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                // Live waveform
                val waveHeights = listOf(wave1, wave2, wave3, wave4, wave5)
                Canvas(
                    modifier = Modifier
                        .width(30.dp)
                        .height(16.dp)
                ) {
                    val barWidth = size.width / 9f
                    val gap = barWidth
                    val maxHeight = size.height
                    waveHeights.forEachIndexed { i, fraction ->
                        val barHeight = maxHeight * fraction
                        val x = i * (barWidth + gap)
                        drawRoundRect(
                            color = activeColor,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x, (maxHeight - barHeight) / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                        )
                    }
                }

                // Timer
                val minutes = durationSeconds / 60
                val seconds = durationSeconds % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = activeColor
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Stop
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop dictation",
                    tint = activeColor,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start dictation",
                    tint = secondaryTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Dictate",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = secondaryTextColor.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // "Done" confirmation — text was captured
        AnimatedVisibility(
            visible = showDone,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(400))
        ) {
            Text(
                text = "Text added",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}
