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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.proactivediary.ui.components.VintageLeatherBook
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif

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
        VintageLeatherBook(
            openProgress = openProgress.value,
            modifier = Modifier.size(80.dp)
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
                    .background(MaterialTheme.colorScheme.primary)
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

