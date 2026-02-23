package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif
import kotlin.math.cos
import kotlin.math.sin

private val AccentBlue = Color(0xFF3897F0)
private val FlowerPink = Color(0xFFF48FB1)
private val FlowerYellow = Color(0xFFFFD54F)
private val StemGreen = Color(0xFF81C784)
private val LeafGreen = Color(0xFF66BB6A)

@Composable
fun NotificationFallbackScreen(
    onTurnOn: () -> Unit,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    analyticsService: AnalyticsService
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            analyticsService.logOnboardingNotifGranted()
        }
        onTurnOn()
    }

    // Bloom animation
    val bloomScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bloomScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.08f))

        // Header
        Text(
            text = "You made the first step!",
            style = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Big title
        Text(
            text = "Stay Inspired",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Turn On Notifications button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentBlue)
                .clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onTurnOn()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Turn On Notifications",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4 bullet benefits
        val benefits = listOf(
            "Gentle reminders to write",
            "Make writing a habit",
            "Reflect and analyse",
            "Become a better writer"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            benefits.forEach { benefit ->
                BenefitRow(text = benefit)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Animated flower/vase illustration
        FlowerIllustration(
            modifier = Modifier.size(160.dp),
            bloomProgress = bloomScale.value
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom: Back (left) + Not Now (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
            Text(
                text = "Not Now",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            analyticsService.logOnboardingNotifDenied()
                            onNotNow()
                        }
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bullet dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
private fun FlowerIllustration(
    modifier: Modifier = Modifier,
    bloomProgress: Float
) {
    // Gentle sway animation
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        rotate(swayAngle) {
            // Vase
            val vaseTop = h * 0.7f
            val vaseBottom = h * 0.95f
            val vaseTopWidth = w * 0.2f
            val vaseBottomWidth = w * 0.25f
            val vasePath = Path().apply {
                moveTo(cx - vaseTopWidth, vaseTop)
                lineTo(cx - vaseBottomWidth, vaseBottom)
                lineTo(cx + vaseBottomWidth, vaseBottom)
                lineTo(cx + vaseTopWidth, vaseTop)
                close()
            }
            drawPath(vasePath, color = Color(0xFF8D6E63).copy(alpha = 0.3f))
            drawPath(vasePath, color = Color(0xFF8D6E63).copy(alpha = 0.5f), style = Stroke(1.5f))

            // Stem
            drawLine(
                color = StemGreen,
                start = Offset(cx, vaseTop),
                end = Offset(cx, h * 0.28f),
                strokeWidth = 3f
            )

            // Leaves
            val leafY = h * 0.55f
            // Left leaf
            val leftLeaf = Path().apply {
                moveTo(cx, leafY)
                quadraticTo(cx - w * 0.15f, leafY - h * 0.06f, cx - w * 0.08f, leafY - h * 0.1f)
                quadraticTo(cx - w * 0.02f, leafY - h * 0.04f, cx, leafY)
            }
            drawPath(leftLeaf, color = LeafGreen.copy(alpha = 0.5f * bloomProgress))

            // Right leaf
            val rightLeafY = h * 0.48f
            val rightLeaf = Path().apply {
                moveTo(cx, rightLeafY)
                quadraticTo(cx + w * 0.14f, rightLeafY - h * 0.05f, cx + w * 0.07f, rightLeafY - h * 0.09f)
                quadraticTo(cx + w * 0.02f, rightLeafY - h * 0.03f, cx, rightLeafY)
            }
            drawPath(rightLeaf, color = LeafGreen.copy(alpha = 0.5f * bloomProgress))

            // Flower petals (bloom animation)
            val flowerCx = cx
            val flowerCy = h * 0.25f
            val petalRadius = w * 0.1f * bloomProgress
            val petalCount = 6

            for (i in 0 until petalCount) {
                val angle = (i * 360f / petalCount) * (Math.PI / 180f)
                val petalCx = flowerCx + (petalRadius * 0.8f * cos(angle)).toFloat()
                val petalCy = flowerCy + (petalRadius * 0.8f * sin(angle)).toFloat()
                drawCircle(
                    color = FlowerPink.copy(alpha = 0.5f * bloomProgress),
                    radius = petalRadius * 0.7f,
                    center = Offset(petalCx, petalCy)
                )
            }

            // Flower center
            drawCircle(
                color = FlowerYellow.copy(alpha = 0.8f * bloomProgress),
                radius = petalRadius * 0.45f,
                center = Offset(flowerCx, flowerCy)
            )
        }
    }
}
