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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.background

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

    val bgColor = DiaryThemeConfig.colorForKey(colorKey)
    val textColor = DiaryThemeConfig.textColorFor(colorKey)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(colorKey)

    // --- Entrance animatables ---
    val bgAlpha = remember { Animatable(0f) }
    val emojiAlpha = remember { Animatable(0f) }
    val emojiEntryScale = remember { Animatable(0.8f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(with(density) { 20.dp.toPx() }) }
    val subtitleAlpha = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val ringEntryScale = remember { Animatable(0.9f) }
    val skipAlpha = remember { Animatable(0f) }

    // --- Exit animatables ---
    val ringFlashAlpha = remember { Animatable(0f) }
    val exitContentAlpha = remember { Animatable(1f) }
    val exitBgAlpha = remember { Animatable(1f) }

    // --- Breathing animation for the emoji ---
    val breathTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by breathTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // --- Staggered entrance ---
    LaunchedEffect(Unit) {
        bgAlpha.animateTo(1f, animationSpec = tween(600))
    }

    LaunchedEffect(Unit) {
        delay(300)
        launch { emojiAlpha.animateTo(1f, animationSpec = tween(500)) }
        launch {
            emojiEntryScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(600)
        launch { titleAlpha.animateTo(1f, animationSpec = tween(500)) }
        launch { titleOffsetY.animateTo(0f, animationSpec = tween(500, easing = EaseOut)) }
    }

    LaunchedEffect(Unit) {
        delay(900)
        subtitleAlpha.animateTo(1f, animationSpec = tween(400))
    }

    LaunchedEffect(Unit) {
        delay(1200)
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
        delay(1500)
        skipAlpha.animateTo(1f, animationSpec = tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = bgAlpha.value * exitBgAlpha.value }
            .background(bgColor)
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
            // Hourglass emoji — small, colorful, like WhatsApp
            Text(
                text = "\u23F3",
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = emojiAlpha.value * exitContentAlpha.value
                    scaleX = emojiEntryScale.value * breathScale
                    scaleY = emojiEntryScale.value * breathScale
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "5 minutes" — hero typography
            Text(
                text = "5 minutes",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp,
                    color = textColor
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value * exitContentAlpha.value
                    translationY = titleOffsetY.value
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle — white as requested
            Text(
                text = "Just you and the page.",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = textColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value * exitContentAlpha.value
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // "Begin" ring button — larger
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        alpha = ringAlpha.value * exitContentAlpha.value
                        scaleX = ringEntryScale.value
                        scaleY = ringEntryScale.value
                    }
                    .then(
                        if (ringFlashAlpha.value > 0f) {
                            Modifier.background(
                                textColor.copy(alpha = ringFlashAlpha.value * 0.3f),
                                CircleShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .border(1.5.dp, textColor.copy(alpha = 0.4f), CircleShape)
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
                            // All content fades out
                            launch {
                                exitContentAlpha.animateTo(0f, animationSpec = tween(500))
                            }
                            // Background fades out with slight delay
                            launch {
                                delay(200)
                                exitBgAlpha.animateTo(
                                    0f,
                                    animationSpec = tween(500, easing = EaseOut)
                                )
                            }
                            // Callback after animations settle
                            delay(750)
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
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = 0.9f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Skip" — subtle
            Text(
                text = "Skip",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = secondaryTextColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .graphicsLayer { alpha = skipAlpha.value * exitContentAlpha.value }
                    .clickable { onSkip() }
            )
        }
    }
}
