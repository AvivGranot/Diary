package com.proactivediary.ui.sydney

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AccentBlue = Color(0xFF3B82F6)
private val DarkSurface = Color(0xFF1A1A1A)
private val DimBackground = Color(0xCC000000)

@Composable
fun SydneyOverlayScreen(
    viewModel: SydneyCaptureViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.captureState) {
        if (state.captureState == CaptureState.Dismissed) {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DimBackground)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { viewModel.cancel() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = state.captureState != CaptureState.Dismissed,
            enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.9f)
        ) {
            CaptureCard(
                state = state,
                onStop = { viewModel.stopAndSave() },
                onCancel = { viewModel.cancel() }
            )
        }
    }
}

@Composable
private fun CaptureCard(
    state: SydneyUiState,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .background(DarkSurface, RoundedCornerShape(24.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* absorb clicks so background dismissal doesn't fire */ }
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Sydney",
            color = AccentBlue,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(20.dp))

        when (state.captureState) {
            CaptureState.Listening -> ListeningContent(state, onStop)
            CaptureState.Processing -> ProcessingContent()
            CaptureState.Saved -> SavedContent()
            CaptureState.Error -> ErrorContent(state.errorMessage)
            CaptureState.Dismissed -> { /* nothing */ }
        }

        // Cancel button (only during listening)
        if (state.captureState == CaptureState.Listening) {
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ListeningContent(state: SydneyUiState, onStop: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Pulsing mic circle
    Box(contentAlignment = Alignment.Center) {
        // Glow ring
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
                .background(AccentBlue.copy(alpha = glowAlpha), CircleShape)
        )
        // Mic button
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(56.dp)
                .background(AccentBlue, CircleShape)
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Stop",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    // Status text
    val displayText = when {
        state.partialTranscript.isNotBlank() -> state.partialTranscript
        state.finalTranscript.isNotBlank() -> state.finalTranscript
        else -> null
    }

    if (displayText != null) {
        Text(
            text = displayText,
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )
    } else {
        Text(
            text = "Listening...",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun ProcessingContent() {
    Text(
        text = "Saving...",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 14.sp
    )
}

@Composable
private fun SavedContent() {
    Icon(
        Icons.Default.Check,
        contentDescription = "Saved",
        tint = AccentBlue,
        modifier = Modifier.size(48.dp)
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Got it",
        color = AccentBlue,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ErrorContent(message: String?) {
    Text(
        text = message ?: "Something went wrong",
        color = Color(0xFFEF4444),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}
