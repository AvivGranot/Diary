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
 * Animated diary that opens and stays open, drawn with lines.
 * Cover pivots from the spine and reveals lined pages inside.
 */
@Composable
private fun OpenDiaryIllustration(
    modifier: Modifier = Modifier,
    openProgress: Float
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pageColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    val coverColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)

    val lineStroke = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        val bookW = w * 0.8f
        val bookH = h * 0.85f
        val bookL = (w - bookW) / 2
        val bookR = bookL + bookW
        val bookT = (h - bookH) / 2
        val bookB = bookT + bookH
        val spine = bookL + bookW * 0.04f

        // ── Back cover (always visible) ──
        drawRoundRect(
            color = coverColor,
            topLeft = Offset(bookL, bookT),
            size = Size(bookW, bookH),
            cornerRadius = CornerRadius(8f)
        )
        drawRoundRect(
            color = inkColor.copy(alpha = 0.35f),
            topLeft = Offset(bookL, bookT),
            size = Size(bookW, bookH),
            cornerRadius = CornerRadius(8f),
            style = lineStroke
        )

        // ── Pages (visible as diary opens) ──
        val pageAlpha = (openProgress * 1.2f).coerceIn(0f, 1f)
        val pageInset = 6f
        drawRoundRect(
            color = pageColor.copy(alpha = pageAlpha),
            topLeft = Offset(spine + pageInset, bookT + pageInset),
            size = Size(bookR - spine - pageInset * 2, bookH - pageInset * 2),
            cornerRadius = CornerRadius(4f)
        )

        // Page lines — drawn progressively
        if (openProgress > 0.3f) {
            val lineAlpha = ((openProgress - 0.3f) / 0.7f).coerceIn(0f, 1f)
            val lineL = spine + bookW * 0.12f
            val lineR = bookR - bookW * 0.08f
            val lineCount = 7
            for (i in 0 until lineCount) {
                val frac = (i + 1).toFloat() / (lineCount + 1)
                val y = bookT + bookH * frac
                // Animated line drawing
                val thisLineProgress = ((lineAlpha - i * 0.08f) / 0.5f).coerceIn(0f, 1f)
                val currentLineR = lineL + (lineR - lineL) * thisLineProgress
                drawLine(
                    color = inkColor.copy(alpha = 0.15f * lineAlpha),
                    start = Offset(lineL, y),
                    end = Offset(currentLineR, y),
                    strokeWidth = 1f
                )
            }

            // Small "writing" marks on first few lines
            if (openProgress > 0.6f) {
                val writingAlpha = ((openProgress - 0.6f) / 0.4f).coerceIn(0f, 1f) * 0.12f
                for (i in 0..2) {
                    val frac = (i + 1).toFloat() / (lineCount + 1)
                    val y = bookT + bookH * frac
                    val markEnd = lineL + (lineR - lineL) * (0.3f + i * 0.15f)
                    drawLine(
                        color = inkColor.copy(alpha = writingAlpha),
                        start = Offset(lineL, y - 2),
                        end = Offset(markEnd, y - 2),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // ── Front cover — rotates open from spine ──
        val coverAngle = -openProgress * 155f
        rotate(
            degrees = coverAngle,
            pivot = Offset(spine, (bookT + bookB) / 2)
        ) {
            // Cover fill
            drawRoundRect(
                color = coverColor.copy(alpha = 0.9f),
                topLeft = Offset(spine, bookT),
                size = Size(bookR - spine, bookH),
                cornerRadius = CornerRadius(6f)
            )
            // Cover outline
            drawRoundRect(
                color = inkColor.copy(alpha = 0.45f),
                topLeft = Offset(spine, bookT),
                size = Size(bookR - spine, bookH),
                cornerRadius = CornerRadius(6f),
                style = lineStroke
            )

            // Diary title label on cover
            val labelW = (bookR - spine) * 0.45f
            val labelH = bookH * 0.06f
            val labelX = spine + (bookR - spine - labelW) / 2
            val labelY = bookT + bookH * 0.38f
            drawRoundRect(
                color = inkColor.copy(alpha = 0.12f),
                topLeft = Offset(labelX, labelY),
                size = Size(labelW, labelH),
                cornerRadius = CornerRadius(3f)
            )

            // Decorative line below label
            drawLine(
                color = inkColor.copy(alpha = 0.08f),
                start = Offset(labelX + labelW * 0.1f, labelY + labelH + 8),
                end = Offset(labelX + labelW * 0.9f, labelY + labelH + 8),
                strokeWidth = 1f
            )
        }

        // ── Spine line ──
        drawLine(
            color = inkColor.copy(alpha = 0.5f),
            start = Offset(spine, bookT + 2),
            end = Offset(spine, bookB - 2),
            strokeWidth = 2f
        )

        // ── Bookmark ribbon (peeks out from top) ──
        if (openProgress > 0.5f) {
            val ribbonAlpha = ((openProgress - 0.5f) / 0.5f).coerceIn(0f, 1f) * 0.4f
            val ribbonX = spine + (bookR - spine) * 0.7f
            val ribbonTop = bookT - 4
            val ribbonBottom = bookT + bookH * 0.18f
            drawLine(
                color = Color(0xFFEF5350).copy(alpha = ribbonAlpha),
                start = Offset(ribbonX, ribbonTop),
                end = Offset(ribbonX, ribbonBottom),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Ribbon V-notch
            drawLine(
                color = Color(0xFFEF5350).copy(alpha = ribbonAlpha),
                start = Offset(ribbonX - 3, ribbonBottom),
                end = Offset(ribbonX, ribbonBottom - 4),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFFEF5350).copy(alpha = ribbonAlpha),
                start = Offset(ribbonX + 3, ribbonBottom),
                end = Offset(ribbonX, ribbonBottom - 4),
                strokeWidth = 2f
            )
        }
    }
}
