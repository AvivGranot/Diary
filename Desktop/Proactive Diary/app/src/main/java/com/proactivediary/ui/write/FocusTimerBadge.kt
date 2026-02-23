package com.proactivediary.ui.write

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TOTAL_SECONDS = 300f

@Composable
fun FocusTimerBadge(
    remainingSeconds: Int,
    colorKey: String,
    onComplete: () -> Unit
) {
    val isComplete = remainingSeconds <= 0

    // Glow pulse for completion
    val glowAlpha = remember { Animatable(0f) }
    // Shrink-to-nothing dismissal
    val capsuleScale = remember { Animatable(1f) }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            // Glow pulse
            launch {
                glowAlpha.animateTo(0.3f, animationSpec = tween(300))
                glowAlpha.animateTo(0f, animationSpec = tween(300))
            }
            // Wait then shrink to nothing
            delay(2000)
            capsuleScale.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 300f
                )
            )
            onComplete()
        }
    }

    val capsuleColor = Color(0xFF1C1C1E).copy(alpha = 0.92f)
    val borderColor = Color.White.copy(alpha = 0.08f)

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = capsuleScale.value
                scaleY = capsuleScale.value
                alpha = capsuleScale.value
            }
            .then(
                if (glowAlpha.value > 0f) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.White.copy(alpha = glowAlpha.value),
                        spotColor = Color.White.copy(alpha = glowAlpha.value)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                color = capsuleColor,
                shape = RoundedCornerShape(20.dp)
            )
            .border(0.5.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isComplete) {
            // Checkmark icon
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Complete",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
        } else {
            // Circular progress ring
            val sweepAngle = (remainingSeconds / TOTAL_SECONDS) * 360f
            val trackColor = Color.White.copy(alpha = 0.15f)
            val progressColor = Color.White.copy(alpha = 0.8f)

            Canvas(modifier = Modifier.size(14.dp)) {
                val strokeWidth = 1.5.dp.toPx()
                val arcSize = size.minDimension - strokeWidth
                val topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - arcSize) / 2f,
                    (size.height - arcSize) / 2f
                )
                val arcSizeObj = androidx.compose.ui.geometry.Size(arcSize, arcSize)

                // Track (full circle)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSizeObj,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Progress arc (empties as time passes)
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSizeObj,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Countdown text
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            Text(
                text = "%d:%02d".format(minutes, seconds),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}
