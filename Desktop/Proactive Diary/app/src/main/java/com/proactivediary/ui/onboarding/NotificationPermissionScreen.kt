package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        // App icon placeholder (simple circle with bell)
        NotificationIcon(modifier = Modifier.size(80.dp))

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(48.dp))

        // Two tab buttons side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Allow button (blue, highlighted)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AllowBlue)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }

            // Don't Allow button (outlined)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.4f))
    }
}

@Composable
private fun NotificationIcon(modifier: Modifier = Modifier) {
    val inkColor = MaterialTheme.colorScheme.onBackground

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Simple bell icon
        val bellWidth = w * 0.45f
        val bellHeight = h * 0.38f
        val bellTop = cy - bellHeight * 0.4f
        val bellLeft = cx - bellWidth / 2

        // Bell dome
        drawRoundRect(
            color = inkColor.copy(alpha = 0.12f),
            topLeft = androidx.compose.ui.geometry.Offset(bellLeft, bellTop),
            size = androidx.compose.ui.geometry.Size(bellWidth, bellHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bellWidth * 0.3f)
        )
        drawRoundRect(
            color = inkColor.copy(alpha = 0.5f),
            topLeft = androidx.compose.ui.geometry.Offset(bellLeft, bellTop),
            size = androidx.compose.ui.geometry.Size(bellWidth, bellHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bellWidth * 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )

        // Bell clapper
        drawCircle(
            color = inkColor.copy(alpha = 0.4f),
            radius = w * 0.03f,
            center = androidx.compose.ui.geometry.Offset(cx, bellTop + bellHeight + w * 0.04f)
        )

        // Bell handle
        drawCircle(
            color = inkColor.copy(alpha = 0.4f),
            radius = w * 0.035f,
            center = androidx.compose.ui.geometry.Offset(cx, bellTop - w * 0.02f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}
