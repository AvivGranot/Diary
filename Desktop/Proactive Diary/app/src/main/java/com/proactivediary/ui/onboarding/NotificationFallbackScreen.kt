package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif

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

    // Diary open animation
    val openProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        openProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2000, delayMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        // Header
        Text(
            text = "You made the first step!",
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Big title
        Text(
            text = "Stay Inspired",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Turn On Notifications button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 bullet benefits
        val benefits = listOf(
            "Gentle reminders to write",
            "Make writing a habit",
            "Reflect and analyse",
            "Become a better writer"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            benefits.forEach { benefit ->
                BenefitRow(text = benefit)
            }
        }

        Spacer(modifier = Modifier.weight(0.04f))

        // Animated diary that opens — line-drawn
        OpenDiaryIllustration(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxWidth(0.6f),
            openProgress = openProgress.value
        )

        Spacer(modifier = Modifier.weight(0.02f))

        // Bottom: Back (left) + Not Now (right) — above system nav bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                style = TextStyle(
                    fontSize = 14.sp,
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
                    fontSize = 14.sp,
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
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 18.sp
            )
        )
    }
}

/**
 * Thick ancient leather-bound book opening flat from the middle.
 * Brown covers, gold accents, visible page thickness, writing marks.
 */
@Composable
private fun OpenDiaryIllustration(
    modifier: Modifier = Modifier,
    openProgress: Float
) {
    val leather = Color(0xFF5D4037)
    val leatherDark = Color(0xFF3E2723)
    val pageEdge = Color(0xFFF5E6D0)
    val pageCream = Color(0xFFFAF3E8)
    val goldAccent = Color(0xFFD4A843)
    val spineColor = Color(0xFF4E342E)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val bookT = h * 0.04f
        val bookB = h * 0.96f
        val bookH = bookB - bookT
        val halfW = w * 0.46f
        val pageThick = bookH * 0.06f

        // ── Page blocks ──
        val pa = (openProgress * 1.5f).coerceIn(0f, 1f)
        val leftL = cx - halfW + 5; val leftR = cx - 3
        val rightL = cx + 3; val rightR = cx + halfW - 5

        // Left page block
        drawRect(pageCream.copy(alpha = pa * 0.9f), Offset(leftL, bookT + 3), Size(leftR - leftL, bookH - 6))
        for (i in 0 until 20) {
            val y = bookB - pageThick + pageThick * (i / 20f)
            drawLine(pageEdge.copy(alpha = pa * (0.2f + i * 0.03f)), Offset(leftL, y), Offset(leftR, y), 0.5f)
        }
        // Right page block
        drawRect(pageCream.copy(alpha = pa * 0.9f), Offset(rightL, bookT + 3), Size(rightR - rightL, bookH - 6))
        for (i in 0 until 20) {
            val y = bookB - pageThick + pageThick * (i / 20f)
            drawLine(pageEdge.copy(alpha = pa * (0.2f + i * 0.03f)), Offset(rightL, y), Offset(rightR, y), 0.5f)
        }

        // Text lines — progressive drawing
        if (openProgress > 0.35f) {
            val la = ((openProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
            for (i in 0..6) {
                val y = bookT + bookH * (0.1f + i * 0.1f)
                val lp = ((la - i * 0.05f) / 0.4f).coerceIn(0f, 1f)
                // Left: spine outward
                drawLine(leatherDark.copy(alpha = 0.15f * la), Offset(leftR - 3, y), Offset(leftR - 3 - (leftR - leftL - 12) * lp, y), 0.7f)
                // Right: spine outward
                drawLine(leatherDark.copy(alpha = 0.15f * la), Offset(rightL + 3, y), Offset(rightL + 3 + (rightR - rightL - 12) * lp, y), 0.7f)
            }

            // Writing marks (thicker)
            if (openProgress > 0.6f) {
                val wa = ((openProgress - 0.6f) / 0.4f).coerceIn(0f, 1f) * 0.12f
                for (i in 0..3) {
                    val y = bookT + bookH * (0.1f + i * 0.1f)
                    val markLen = (leftR - leftL - 12) * (0.35f + i * 0.12f)
                    drawLine(leatherDark.copy(alpha = wa), Offset(leftR - 3, y - 2), Offset(leftR - 3 - markLen, y - 2), 1.5f)
                }
                for (i in 0..2) {
                    val y = bookT + bookH * (0.1f + i * 0.1f)
                    val markLen = (rightR - rightL - 12) * (0.3f + i * 0.1f)
                    drawLine(leatherDark.copy(alpha = wa * 0.8f), Offset(rightL + 3, y - 2), Offset(rightL + 3 + markLen, y - 2), 1.5f)
                }
            }
        }

        // ── Left cover ──
        rotate(openProgress * 170f, Offset(cx, (bookT + bookB) / 2)) {
            drawRoundRect(leather, Offset(cx - halfW, bookT), Size(halfW, bookH), CornerRadius(4f))
            drawRoundRect(leatherDark, Offset(cx - halfW, bookT), Size(halfW, bookH), CornerRadius(4f), style = Stroke(1.5f))
            drawRoundRect(goldAccent.copy(alpha = 0.3f), Offset(cx - halfW + 5, bookT + 5), Size(halfW - 10, bookH - 10), CornerRadius(2f), style = Stroke(0.7f))
        }

        // ── Right cover ──
        rotate(-openProgress * 170f, Offset(cx, (bookT + bookB) / 2)) {
            drawRoundRect(leather, Offset(cx, bookT), Size(halfW, bookH), CornerRadius(4f))
            drawRoundRect(leatherDark, Offset(cx, bookT), Size(halfW, bookH), CornerRadius(4f), style = Stroke(1.5f))
            drawRoundRect(goldAccent.copy(alpha = 0.3f), Offset(cx + 5, bookT + 5), Size(halfW - 10, bookH - 10), CornerRadius(2f), style = Stroke(0.7f))
            val lw = halfW * 0.5f; val lh = bookH * 0.06f
            drawRoundRect(goldAccent.copy(alpha = 0.2f), Offset(cx + (halfW - lw) / 2, bookT + bookH * 0.4f), Size(lw, lh), CornerRadius(2f))
            drawRoundRect(goldAccent.copy(alpha = 0.4f), Offset(cx + (halfW - lw) / 2, bookT + bookH * 0.4f), Size(lw, lh), CornerRadius(2f), style = Stroke(0.7f))
        }

        // ── Spine with ridges ──
        drawLine(spineColor, Offset(cx, bookT), Offset(cx, bookB), 3f)
        for (i in 1..4) drawLine(leatherDark.copy(alpha = 0.5f), Offset(cx - 3, bookT + bookH * i / 5f), Offset(cx + 3, bookT + bookH * i / 5f), 1.5f)

    }
}
