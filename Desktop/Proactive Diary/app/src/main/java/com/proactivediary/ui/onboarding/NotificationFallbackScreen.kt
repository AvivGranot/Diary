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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif

private val AccentBlue = Color(0xFF3897F0)

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
                .background(AccentBlue.copy(alpha = 0.6f))
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
 * Diary that opens from the middle like a real book — both covers spread outward.
 * Pages revealed with progressive line drawing and writing marks.
 */
@Composable
private fun OpenDiaryIllustration(
    modifier: Modifier = Modifier,
    openProgress: Float
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pageColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    val coverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f)
    val lineStroke = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val bookT = h * 0.06f
        val bookB = h * 0.94f
        val bookH = bookB - bookT
        val halfW = w * 0.44f

        // ── Left page ──
        val pageAlpha = (openProgress * 1.3f).coerceIn(0f, 1f)
        drawRoundRect(
            color = pageColor.copy(alpha = pageAlpha),
            topLeft = Offset(cx - halfW + 4, bookT + 3),
            size = Size(halfW - 6, bookH - 6),
            cornerRadius = CornerRadius(3f)
        )
        // Right page
        drawRoundRect(
            color = pageColor.copy(alpha = pageAlpha),
            topLeft = Offset(cx + 2, bookT + 3),
            size = Size(halfW - 6, bookH - 6),
            cornerRadius = CornerRadius(3f)
        )

        // Page lines — drawn progressively on both sides
        if (openProgress > 0.35f) {
            val lineAlpha = ((openProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
            val lineCount = 6
            for (i in 0 until lineCount) {
                val y = bookT + bookH * (0.15f + i * 0.12f)
                val thisProgress = ((lineAlpha - i * 0.06f) / 0.5f).coerceIn(0f, 1f)

                // Left page lines (draw from spine outward)
                val leftStart = cx - 5
                val leftEnd = leftStart - (halfW - 16) * thisProgress
                drawLine(inkColor.copy(alpha = 0.15f * lineAlpha), Offset(leftStart, y), Offset(leftEnd, y), 0.8f)

                // Right page lines (draw from spine outward)
                val rightStart = cx + 5
                val rightEnd = rightStart + (halfW - 16) * thisProgress
                drawLine(inkColor.copy(alpha = 0.15f * lineAlpha), Offset(rightStart, y), Offset(rightEnd, y), 0.8f)
            }

            // Writing marks on left page
            if (openProgress > 0.6f) {
                val wa = ((openProgress - 0.6f) / 0.4f).coerceIn(0f, 1f) * 0.13f
                for (i in 0..3) {
                    val y = bookT + bookH * (0.15f + i * 0.12f)
                    val markLen = (halfW - 16) * (0.4f + i * 0.12f)
                    drawLine(inkColor.copy(alpha = wa), Offset(cx - 5, y - 2), Offset(cx - 5 - markLen, y - 2), 1.5f)
                }
            }

            // Writing marks on right page
            if (openProgress > 0.7f) {
                val wa = ((openProgress - 0.7f) / 0.3f).coerceIn(0f, 1f) * 0.1f
                for (i in 0..2) {
                    val y = bookT + bookH * (0.15f + i * 0.12f)
                    val markLen = (halfW - 16) * (0.35f + i * 0.1f)
                    drawLine(inkColor.copy(alpha = wa), Offset(cx + 5, y - 2), Offset(cx + 5 + markLen, y - 2), 1.5f)
                }
            }
        }

        // ── Left cover — pivots open from center spine ──
        val leftAngle = openProgress * 165f
        rotate(degrees = leftAngle, pivot = Offset(cx, (bookT + bookB) / 2)) {
            drawRoundRect(
                color = coverColor.copy(alpha = 0.85f),
                topLeft = Offset(cx - halfW, bookT),
                size = Size(halfW, bookH),
                cornerRadius = CornerRadius(5f)
            )
            drawRoundRect(
                color = inkColor.copy(alpha = 0.4f),
                topLeft = Offset(cx - halfW, bookT),
                size = Size(halfW, bookH),
                cornerRadius = CornerRadius(5f),
                style = lineStroke
            )
        }

        // ── Right cover — pivots open from center spine ──
        val rightAngle = -openProgress * 165f
        rotate(degrees = rightAngle, pivot = Offset(cx, (bookT + bookB) / 2)) {
            drawRoundRect(
                color = coverColor.copy(alpha = 0.85f),
                topLeft = Offset(cx, bookT),
                size = Size(halfW, bookH),
                cornerRadius = CornerRadius(5f)
            )
            drawRoundRect(
                color = inkColor.copy(alpha = 0.4f),
                topLeft = Offset(cx, bookT),
                size = Size(halfW, bookH),
                cornerRadius = CornerRadius(5f),
                style = lineStroke
            )
            // Title label on front cover
            val labelW = halfW * 0.5f
            val labelH = bookH * 0.06f
            val labelX = cx + (halfW - labelW) / 2
            val labelY = bookT + bookH * 0.42f
            drawRoundRect(
                color = inkColor.copy(alpha = 0.1f),
                topLeft = Offset(labelX, labelY),
                size = Size(labelW, labelH),
                cornerRadius = CornerRadius(2f)
            )
        }

        // ── Center spine ──
        drawLine(
            color = inkColor.copy(alpha = 0.5f),
            start = Offset(cx, bookT),
            end = Offset(cx, bookB),
            strokeWidth = 2f
        )

        // ── Bookmark ribbon ──
        if (openProgress > 0.5f) {
            val ra = ((openProgress - 0.5f) / 0.5f).coerceIn(0f, 1f) * 0.45f
            val rx = cx + halfW * 0.55f
            val ribbonBot = bookT + bookH * 0.16f
            drawLine(Color(0xFFEF5350).copy(alpha = ra), Offset(rx, bookT - 2), Offset(rx, ribbonBot), 2.5f)
            // V-notch
            drawLine(Color(0xFFEF5350).copy(alpha = ra), Offset(rx - 3, ribbonBot), Offset(rx, ribbonBot - 4), 1.5f)
            drawLine(Color(0xFFEF5350).copy(alpha = ra), Offset(rx + 3, ribbonBot), Offset(rx, ribbonBot - 4), 1.5f)
        }
    }
}
