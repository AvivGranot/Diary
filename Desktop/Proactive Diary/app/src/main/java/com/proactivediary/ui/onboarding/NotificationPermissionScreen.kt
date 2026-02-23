package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif

private val AllowBlue = Color(0xFF3897F0)

@Composable
fun NotificationPermissionScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    analyticsService: AnalyticsService
) {
    LaunchedEffect(Unit) {
        analyticsService.logOnboardingNotifShown()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            analyticsService.logOnboardingNotifGranted()
            onContinue()
        } else {
            analyticsService.logOnboardingNotifDenied()
            onSkip()
        }
    }

    // Animated border phase for "Allow" button
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val borderPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderPhase"
    )

    // Diary open animation
    val openProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        openProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1500, delayMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.25f))

        // Animated diary icon that opens
        DiaryOpenIcon(
            modifier = Modifier.size(80.dp),
            openProgress = openProgress.value
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = "Allow Proactive Diary to\nsend you notifications?",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Two tab buttons side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Allow button — blue fill + animated border highlight
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AllowBlue)
                    .drawBehind {
                        // Animated scanning line around border
                        val cornerR = 8.dp.toPx()
                        val borderPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    Rect(0f, 0f, size.width, size.height),
                                    CornerRadius(cornerR)
                                )
                            )
                        }
                        val measure = PathMeasure()
                        measure.setPath(borderPath, true)
                        val totalLen = measure.length
                        val segLen = totalLen * 0.25f
                        val offset = totalLen * borderPhase

                        drawPath(
                            path = borderPath,
                            color = Color.White.copy(alpha = 0.8f),
                            style = Stroke(
                                width = 2.5f,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(segLen, totalLen - segLen),
                                    offset
                                )
                            )
                        )
                    }
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            analyticsService.logOnboardingNotifGranted()
                            onContinue()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Allow",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }

            // Don't Allow button (outlined)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        analyticsService.logOnboardingNotifDenied()
                        onSkip()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Don't Allow",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.35f))
    }
}

/**
 * Thick ancient leather-bound book opening flat from the middle.
 * Brown covers, gold accents, visible page thickness on both sides.
 */
@Composable
private fun DiaryOpenIcon(
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
        val bookT = h * 0.05f
        val bookB = h * 0.95f
        val bookH = bookB - bookT
        val halfW = w * 0.42f
        val pageThick = bookH * 0.055f

        // ── Page blocks (thick stack of pages) ──
        val pa = (openProgress * 1.5f).coerceIn(0f, 1f)
        val leftL = cx - halfW + 5; val leftR = cx - 3
        val rightL = cx + 3; val rightR = cx + halfW - 5

        // Left pages
        drawRect(pageCream.copy(alpha = pa * 0.9f), Offset(leftL, bookT + 3), Size(leftR - leftL, bookH - 6))
        for (i in 0 until 18) {
            val y = bookB - pageThick + pageThick * (i / 18f)
            drawLine(pageEdge.copy(alpha = pa * (0.25f + i * 0.03f)), Offset(leftL, y), Offset(leftR, y), 0.5f)
        }
        // Right pages
        drawRect(pageCream.copy(alpha = pa * 0.9f), Offset(rightL, bookT + 3), Size(rightR - rightL, bookH - 6))
        for (i in 0 until 18) {
            val y = bookB - pageThick + pageThick * (i / 18f)
            drawLine(pageEdge.copy(alpha = pa * (0.25f + i * 0.03f)), Offset(rightL, y), Offset(rightR, y), 0.5f)
        }

        // Text lines
        if (openProgress > 0.5f) {
            val la = ((openProgress - 0.5f) / 0.5f).coerceIn(0f, 1f) * 0.18f
            for (i in 0..4) {
                val y = bookT + bookH * (0.12f + i * 0.13f)
                drawLine(leatherDark.copy(alpha = la), Offset(leftL + 6, y), Offset(leftR - 3, y), 0.7f)
                drawLine(leatherDark.copy(alpha = la * 0.8f), Offset(rightL + 3, y), Offset(rightR - 6, y), 0.7f)
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
            // Title
            val lw = halfW * 0.5f; val lh = bookH * 0.07f
            drawRoundRect(goldAccent.copy(alpha = 0.2f), Offset(cx + (halfW - lw) / 2, bookT + bookH * 0.4f), Size(lw, lh), CornerRadius(2f))
            drawRoundRect(goldAccent.copy(alpha = 0.4f), Offset(cx + (halfW - lw) / 2, bookT + bookH * 0.4f), Size(lw, lh), CornerRadius(2f), style = Stroke(0.7f))
        }

        // ── Spine with ridges ──
        drawLine(spineColor, Offset(cx, bookT), Offset(cx, bookB), 3f)
        for (i in 1..4) drawLine(leatherDark.copy(alpha = 0.5f), Offset(cx - 3, bookT + bookH * i / 5f), Offset(cx + 3, bookT + bookH * i / 5f), 1.5f)

        // ── Bookmark ──
        if (openProgress > 0.4f) {
            val ra = ((openProgress - 0.4f) / 0.6f).coerceIn(0f, 1f) * 0.7f
            drawLine(Color(0xFFC62828).copy(alpha = ra), Offset(cx + halfW * 0.45f, bookT - 3), Offset(cx + halfW * 0.45f, bookT + bookH * 0.17f), 2.5f, StrokeCap.Round)
        }
    }
}
