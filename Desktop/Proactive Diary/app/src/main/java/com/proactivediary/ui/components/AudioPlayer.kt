package com.proactivediary.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AudioPlayer(
    audioFile: File,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    val extendedColors = LocalDiaryExtendedColors.current

    // Initialize MediaPlayer
    LaunchedEffect(audioFile) {
        if (!audioFile.exists()) return@LaunchedEffect
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                duration = this.duration
                setOnCompletionListener {
                    isPlaying = false
                    currentPosition = 0
                }
            }
        } catch (_: Exception) { }
    }

    // Update progress while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                currentPosition = it.currentPosition
            }
            delay(100)
        }
    }

    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Generate pseudo-waveform bars (random but consistent per file)
    val barCount = 48
    val waveformBars = remember(audioFile.name) {
        val seed = audioFile.name.hashCode().toLong()
        val rng = java.util.Random(seed)
        List(barCount) { 0.15f + rng.nextFloat() * 0.85f }
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // Top row: delete button
            if (onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Delete recording",
                        tint = secondaryTextColor.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                mediaPlayer?.release()
                                mediaPlayer = null
                                onDelete()
                            }
                    )
                }
                Spacer(Modifier.height(DiarySpacing.xxs))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                // Play/Pause button — circle with accent
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            mediaPlayer?.let { player ->
                                if (isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                } else {
                                    player.start()
                                    isPlaying = true
                                }
                            }
                        },
                    shape = CircleShape,
                    color = extendedColors.accent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Waveform visualization
                Column(modifier = Modifier.weight(1f)) {
                    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .pointerInput(duration) {
                                if (duration <= 0) return@pointerInput
                                detectHorizontalDragGestures { change, _ ->
                                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                    val seekTo = (fraction * duration).toInt()
                                    mediaPlayer?.seekTo(seekTo)
                                    currentPosition = seekTo
                                }
                            }
                    ) {
                        val barWidth = size.width / (barCount * 1.5f)
                        val gap = barWidth * 0.5f
                        val maxHeight = size.height

                        waveformBars.forEachIndexed { i, amplitude ->
                            val barHeight = maxHeight * amplitude
                            val x = i * (barWidth + gap)
                            val barProgress = (i.toFloat() / barCount)
                            val barColor = if (barProgress <= progress) {
                                extendedColors.accent
                            } else {
                                secondaryTextColor.copy(alpha = 0.2f)
                            }
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, (maxHeight - barHeight) / 2),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2)
                            )
                        }
                    }

                    // Timer in serif
                    val currentSec = currentPosition / 1000
                    val durationSec = duration / 1000
                    Text(
                        text = String.format(
                            "%d:%02d / %d:%02d",
                            currentSec / 60, currentSec % 60,
                            durationSec / 60, durationSec % 60
                        ),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = InstrumentSerif
                        ),
                        color = secondaryTextColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = DiarySpacing.xxs)
                    )
                }
            }
        }
    }
}
