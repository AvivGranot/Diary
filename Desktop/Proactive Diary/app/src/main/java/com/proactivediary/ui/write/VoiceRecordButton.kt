package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import kotlinx.coroutines.delay

/**
 * Dictation button — Apple Voice Memos style.
 * Active: 12 waveform bars + serif timer + pulsing red dot + stop button.
 * Inactive: mic icon + "Speak your thoughts" label.
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
    val extendedColors = LocalDiaryExtendedColors.current

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
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Pulsing red dot
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    // 12 waveform bars at organic rhythms
    val waveHeights = (0 until 12).map { i ->
        val speed = 250 + (i * 37) % 200
        val lo = 0.2f + (i * 0.05f) % 0.3f
        infiniteTransition.animateFloat(
            initialValue = lo,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(speed), RepeatMode.Reverse),
            label = "w$i"
        )
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) pulseScale else 1f,
        label = "btnScale"
    )

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
                    if (isRecording) extendedColors.accent.copy(alpha = 0.10f)
                    else secondaryTextColor.copy(alpha = 0.08f)
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRecording) onStopRecording() else onStartRecording()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                // Pulsing red dot
                Canvas(modifier = Modifier.size(4.dp)) {
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = dotAlpha),
                        radius = size.minDimension / 2
                    )
                }

                // 12-bar waveform
                Canvas(
                    modifier = Modifier
                        .width(48.dp)
                        .height(24.dp)
                ) {
                    val barCount = 12
                    val barWidth = size.width / (barCount * 2f - 1)
                    val gap = barWidth
                    val maxHeight = size.height
                    waveHeights.forEachIndexed { i, anim ->
                        val fraction = anim.value
                        val barHeight = maxHeight * fraction
                        val x = i * (barWidth + gap)
                        drawRoundRect(
                            color = extendedColors.accent,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x, (maxHeight - barHeight) / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                        )
                    }
                }

                // Timer in serif
                val minutes = durationSeconds / 60
                val seconds = durationSeconds % 60
                Text(
                    text = String.format("%d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = InstrumentSerif
                    ),
                    color = extendedColors.accent
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Stop button — circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(extendedColors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop dictation",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start dictation",
                    tint = secondaryTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Speak your thoughts",
                    style = MaterialTheme.typography.labelMedium,
                    color = secondaryTextColor.copy(alpha = 0.5f)
                )
            }
        }

        // "Words captured" confirmation
        AnimatedVisibility(
            visible = showDone,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(400))
        ) {
            Text(
                text = "Words captured",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = secondaryTextColor.copy(alpha = 0.6f)
            )
        }
    }
}
