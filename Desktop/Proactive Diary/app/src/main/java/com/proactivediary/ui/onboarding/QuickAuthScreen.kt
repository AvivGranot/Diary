package com.proactivediary.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.components.VintageLeatherBook
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans

@Composable
fun QuickAuthScreen(
    onAuthenticated: () -> Unit,
    onSkip: () -> Unit,
    analyticsService: AnalyticsService,
    viewModel: QuickAuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val screenStartTime = remember { System.currentTimeMillis() }

    // Animate the book opening
    val bookProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bookProgress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }

    LaunchedEffect(Unit) {
        analyticsService.logOnboardingStart()
        analyticsService.logOnboardingAuthStart()
    }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            analyticsService.logOnboardingAuthComplete(
                method = "google",
                durationMs = System.currentTimeMillis() - screenStartTime
            )
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = OnboardingHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Top breathing room ──
        Spacer(modifier = Modifier.weight(0.4f))

        // ── Small leather book illustration ──
        VintageLeatherBook(
            openProgress = bookProgress.value,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── App name — big serif, the hero ──
        Text(
            text = "Proactive\nDiary",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 36.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 40.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Tagline ──
        Text(
            text = "Your private space to think, write, and grow.",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        // ── Feature bullets ──
        Spacer(modifier = Modifier.weight(0.4f))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FeatureRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                text = "Send anonymous notes of kindness"
            )
            FeatureRow(
                icon = Icons.Outlined.FormatQuote,
                text = "Discover and share quotes"
            )
            FeatureRow(
                icon = Icons.Outlined.Edit,
                text = "Write your own private journal"
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Google CTA button — ink fill, white text ──
        OnboardingPrimaryButton(
            text = "Continue with Google",
            onClick = { viewModel.signInWithGoogle(context) },
            enabled = !state.isSigningIn,
            isLoading = state.isSigningIn
        )

        // ── Error message ──
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Skip link ──
        Text(
            text = "Skip for now",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        analyticsService.logOnboardingAuthSkip()
                        onSkip()
                    }
                )
                .padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Feature row with bare icon (no circle badge) ──

@Composable
private fun FeatureRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
