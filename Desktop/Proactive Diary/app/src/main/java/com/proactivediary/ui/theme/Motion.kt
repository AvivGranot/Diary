package com.proactivediary.ui.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object DiaryMotion {
    // ── Spring configs ───────────────────────────────────────────────
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessHigh
    )

    // ── Tween configs ────────────────────────────────────────────────
    val QuickFade = tween<Float>(durationMillis = 150)
    val MediumFade = tween<Float>(durationMillis = 300)
    val SlowReveal = tween<Float>(durationMillis = 600)

    // ── Card/button press scales ─────────────────────────────────────
    const val CARD_PRESS_SCALE = 0.97f
    const val BUTTON_PRESS_SCALE = 0.95f

    // ── Stagger delay ────────────────────────────────────────────────
    const val STAGGER_DELAY_MS = 50L

    // ── Navigation transitions (200ms = fast but directional) ────────
    val NavEnterTransition: EnterTransition = fadeIn(tween(200)) +
        slideInHorizontally(tween(200)) { it / 5 }

    val NavExitTransition: ExitTransition = fadeOut(tween(200)) +
        slideOutHorizontally(tween(200)) { -it / 5 }

    val NavPopEnterTransition: EnterTransition = fadeIn(tween(200)) +
        slideInHorizontally(tween(200)) { -it / 5 }

    val NavPopExitTransition: ExitTransition = fadeOut(tween(200)) +
        slideOutHorizontally(tween(200)) { it / 5 }
}

// ── Animated shimmer brush for loading states ───────────────────────
@Composable
fun rememberShimmerBrush(
    baseColor: Color = LocalDiaryExtendedColors.current.shimmerBase,
    highlightColor: Color = LocalDiaryExtendedColors.current.shimmerHighlight,
    shimmerWidth: Float = 800f
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = shimmerWidth * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - shimmerWidth, 0f),
        end = Offset(translateAnim, 0f)
    )
}
