package com.proactivediary.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    // ── Navigation transitions ───────────────────────────────────────
    val NavEnterTransition: EnterTransition = fadeIn(tween(300)) +
        slideInHorizontally(tween(300)) { it / 4 }

    val NavExitTransition: ExitTransition = fadeOut(tween(300)) +
        slideOutHorizontally(tween(300)) { -it / 4 }

    val NavPopEnterTransition: EnterTransition = fadeIn(tween(300)) +
        slideInHorizontally(tween(300)) { -it / 4 }

    val NavPopExitTransition: ExitTransition = fadeOut(tween(300)) +
        slideOutHorizontally(tween(300)) { it / 4 }
}

// ── Shimmer brush for loading states ─────────────────────────────────
@Composable
fun rememberShimmerBrush(
    baseColor: Color = LocalDiaryExtendedColors.current.shimmerBase,
    highlightColor: Color = LocalDiaryExtendedColors.current.shimmerHighlight,
    shimmerWidth: Float = 400f
): Brush {
    return remember(baseColor, highlightColor) {
        Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset.Zero,
            end = Offset(shimmerWidth, shimmerWidth)
        )
    }
}
