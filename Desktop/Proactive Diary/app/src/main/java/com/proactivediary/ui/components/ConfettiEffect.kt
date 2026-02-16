package com.proactivediary.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.proactivediary.ui.theme.DiaryColors
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ConfettiEffect(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    durationMs: Int = 2500,
    colors: List<Color> = listOf(
        DiaryColors.ElectricIndigo,
        DiaryColors.NeonPink,
        DiaryColors.CyberTeal,
        DiaryColors.SunsetOrange,
        DiaryColors.VividPurple,
        DiaryColors.LimeGreen,
        DiaryColors.ElectricBlue
    )
) {
    val progress = remember { Animatable(0f) }

    val particles = remember(trigger) {
        if (!trigger) emptyList()
        else List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                initialY = -Random.nextFloat() * 0.3f,
                velocityX = (Random.nextFloat() - 0.5f) * 0.4f,
                velocityY = Random.nextFloat() * 0.3f + 0.4f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 8f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
            )
        }
    }

    if (trigger && progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress.value

            particles.forEach { particle ->
                val alpha = (1f - t).coerceIn(0f, 1f)
                val gravity = 0.5f
                val px = (particle.x + particle.velocityX * t) * w
                val py = (particle.initialY + particle.velocityY * t + gravity * t * t) * h
                val rot = particle.rotation + particle.rotationSpeed * t

                if (py < h * 1.2f && alpha > 0.05f) {
                    rotate(rot, pivot = Offset(px, py)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(px - particle.size / 2, py - particle.size / 2),
                            size = Size(particle.size, particle.size * 0.6f)
                        )
                    }
                }
            }
        }
    }
}
