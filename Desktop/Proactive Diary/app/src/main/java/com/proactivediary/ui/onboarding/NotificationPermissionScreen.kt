package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.MaterialTheme
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun NotificationPermissionScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    analyticsService: AnalyticsService
) {
    // Track screen shown
    LaunchedEffect(Unit) {
        analyticsService.logOnboardingNotifShown()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            analyticsService.logOnboardingNotifGranted()
        } else {
            analyticsService.logOnboardingNotifDenied()
        }
        onContinue()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        // Illustration: stylized bell with sparkles drawn with Canvas
        NotificationIllustration(
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Headline
        Text(
            text = "Stay Inspired",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Body text
        Text(
            text = "Get gentle reminders to write, plus\npersonalized suggestions based on\nyour routine and surroundings.",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        )

        Spacer(modifier = Modifier.weight(0.25f))

        // Primary button: "Turn On Notifications"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onBackground)
                .clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onContinue()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Turn On Notifications",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.background
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary: "Not Now"
        Text(
            text = "Not Now",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        analyticsService.logOnboardingNotifDenied()
                        onSkip()
                    }
                )
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.weight(0.1f))
    }
}

@Composable
private fun NotificationIllustration(modifier: Modifier = Modifier) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Bell body
        val bellWidth = w * 0.4f
        val bellHeight = h * 0.35f
        val bellTop = cy - bellHeight * 0.3f
        val bellLeft = cx - bellWidth / 2

        // Bell dome (rounded rect)
        drawRoundRect(
            color = pencilColor.copy(alpha = 0.15f),
            topLeft = Offset(bellLeft, bellTop),
            size = Size(bellWidth, bellHeight),
            cornerRadius = CornerRadius(bellWidth * 0.3f)
        )
        drawRoundRect(
            color = inkColor.copy(alpha = 0.6f),
            topLeft = Offset(bellLeft, bellTop),
            size = Size(bellWidth, bellHeight),
            cornerRadius = CornerRadius(bellWidth * 0.3f),
            style = Stroke(width = 2f)
        )

        // Bell clapper (small circle at bottom)
        drawCircle(
            color = inkColor.copy(alpha = 0.5f),
            radius = w * 0.03f,
            center = Offset(cx, bellTop + bellHeight + w * 0.04f)
        )

        // Bell handle (small arc at top)
        drawCircle(
            color = inkColor.copy(alpha = 0.5f),
            radius = w * 0.04f,
            center = Offset(cx, bellTop - w * 0.02f),
            style = Stroke(width = 2f)
        )

        // Sparkle dots around the bell
        val sparkleColor = inkColor.copy(alpha = 0.3f)
        val sparkleRadius = w * 0.015f

        // Top right sparkle
        drawCircle(color = sparkleColor, radius = sparkleRadius, center = Offset(cx + w * 0.25f, cy - h * 0.2f))
        drawCircle(color = sparkleColor, radius = sparkleRadius * 0.7f, center = Offset(cx + w * 0.3f, cy - h * 0.15f))

        // Top left sparkle
        drawCircle(color = sparkleColor, radius = sparkleRadius, center = Offset(cx - w * 0.25f, cy - h * 0.18f))
        drawCircle(color = sparkleColor, radius = sparkleRadius * 0.6f, center = Offset(cx - w * 0.2f, cy - h * 0.25f))

        // Small lines radiating (like notification rays)
        val rayColor = inkColor.copy(alpha = 0.2f)
        // Right ray
        drawLine(
            color = rayColor,
            start = Offset(cx + bellWidth / 2 + 8, bellTop + bellHeight * 0.3f),
            end = Offset(cx + bellWidth / 2 + 20, bellTop + bellHeight * 0.15f),
            strokeWidth = 1.5f
        )
        // Left ray
        drawLine(
            color = rayColor,
            start = Offset(cx - bellWidth / 2 - 8, bellTop + bellHeight * 0.3f),
            end = Offset(cx - bellWidth / 2 - 20, bellTop + bellHeight * 0.15f),
            strokeWidth = 1.5f
        )
    }
}
