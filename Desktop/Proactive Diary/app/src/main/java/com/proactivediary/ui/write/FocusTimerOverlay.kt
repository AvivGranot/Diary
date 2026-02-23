package com.proactivediary.ui.write

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FocusTimerOverlay(
    colorKey: String,
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    // --- Entrance animatables ---
    val bgAlpha = remember { Animatable(0f) }
    val hourglassAlpha = remember { Animatable(0f) }
    val hourglassEntryScale = remember { Animatable(0.8f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(with(density) { 20.dp.toPx() }) }
    val subtitleAlpha = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val ringEntryScale = remember { Animatable(0.9f) }
    val skipAlpha = remember { Animatable(0f) }

    // --- Exit animatables ---
    val ringFlashAlpha = remember { Animatable(0f) }
    val exitTextAlpha = remember { Animatable(1f) }
    val exitHourglassScale = remember { Animatable(1f) }
    val exitHourglassTranslateX = remember { Animatable(0f) }
    val exitHourglassTranslateY = remember { Animatable(0f) }
    val exitBgAlpha = remember { Animatable(1f) }

    // --- Breathing animation (continuous) ---
    val breathTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by breathTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // --- Staggered entrance ---
    LaunchedEffect(Unit) {
        // Step 1: Background
        bgAlpha.animateTo(0.95f, animationSpec = tween(800))

        // Step 2: Hourglass (starts at 400ms from beginning, but bg is already done at 800ms)
        // We use delays relative to the start
    }

    LaunchedEffect(Unit) {
        delay(400)
        launch { hourglassAlpha.animateTo(1f, animationSpec = tween(600)) }
        launch {
            hourglassEntryScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(800)
        launch { titleAlpha.animateTo(1f, animationSpec = tween(500)) }
        launch { titleOffsetY.animateTo(0f, animationSpec = tween(500, easing = EaseOut)) }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        subtitleAlpha.animateTo(1f, animationSpec = tween(400))
    }

    LaunchedEffect(Unit) {
        delay(1500)
        launch { ringAlpha.animateTo(1f, animationSpec = tween(400)) }
        launch {
            ringEntryScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(1800)
        skipAlpha.animateTo(0.2f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = bgAlpha.value * exitBgAlpha.value }
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* consume touches */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            // Hourglass icon — hero element
            Icon(
                imageVector = Icons.Outlined.HourglassTop,
                contentDescription = "Focus timer",
                tint = Color.White,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        alpha = hourglassAlpha.value * exitTextAlpha.value
                        scaleX = hourglassEntryScale.value * breathScale * exitHourglassScale.value
                        scaleY = hourglassEntryScale.value * breathScale * exitHourglassScale.value
                        translationX = exitHourglassTranslateX.value
                        translationY = exitHourglassTranslateY.value
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // "5 minutes" — hero typography
            Text(
                text = "5 minutes",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 48.sp,
                    letterSpacing = 2.sp,
                    color = Color.White
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value * exitTextAlpha.value
                    translationY = titleOffsetY.value
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle — a whisper
            Text(
                text = "Just you and the page.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.35f)
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value * exitTextAlpha.value
                }
            )

            Spacer(modifier = Modifier.height(56.dp))

            // "Begin" ring button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        alpha = ringAlpha.value * exitTextAlpha.value
                        scaleX = ringEntryScale.value
                        scaleY = ringEntryScale.value
                    }
                    .then(
                        if (ringFlashAlpha.value > 0f) {
                            Modifier.background(
                                Color.White.copy(alpha = ringFlashAlpha.value),
                                CircleShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            // Ring flash
                            launch {
                                ringFlashAlpha.animateTo(1f, animationSpec = tween(150))
                                ringFlashAlpha.animateTo(0f, animationSpec = tween(150))
                            }
                            // Text elements fade out
                            launch {
                                exitTextAlpha.animateTo(0f, animationSpec = tween(400))
                            }
                            // Hourglass spring to top-right corner
                            launch {
                                exitHourglassScale.animateTo(
                                    0.25f,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 200f
                                    )
                                )
                            }
                            launch {
                                exitHourglassTranslateX.animateTo(
                                    screenWidthPx * 0.35f,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 200f
                                    )
                                )
                            }
                            launch {
                                exitHourglassTranslateY.animateTo(
                                    -screenHeightPx * 0.4f,
                                    animationSpec = spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 200f
                                    )
                                )
                            }
                            // Background fade out (200ms delay)
                            launch {
                                delay(200)
                                exitBgAlpha.animateTo(
                                    0f,
                                    animationSpec = tween(700, easing = EaseOut)
                                )
                            }
                            // Callback after animations settle
                            delay(950)
                            onStart()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Begin",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "Skip" — nearly invisible
            Text(
                text = "Skip",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .graphicsLayer { alpha = skipAlpha.value * exitTextAlpha.value }
                    .clickable { onSkip() }
            )
        }
    }
}
