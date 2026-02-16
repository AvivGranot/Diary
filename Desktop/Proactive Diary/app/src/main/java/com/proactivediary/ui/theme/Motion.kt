package com.proactivediary.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object DiaryMotion {
    // Spring configs
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

    // Tween configs
    val QuickFade = tween<Float>(durationMillis = 150)
    val MediumFade = tween<Float>(durationMillis = 300)
    val SlowReveal = tween<Float>(durationMillis = 600)

    // Card press scale
    const val CARD_PRESS_SCALE = 0.97f
    const val BUTTON_PRESS_SCALE = 0.95f

    // Stagger delay between items
    const val STAGGER_DELAY_MS = 50L
}
