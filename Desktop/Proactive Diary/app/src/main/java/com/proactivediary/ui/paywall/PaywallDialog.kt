package com.proactivediary.ui.paywall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.components.PrivacyBadge
import com.proactivediary.ui.theme.BottomSheetShape
import com.proactivediary.ui.theme.DiaryMotion
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PillShape
import kotlinx.coroutines.delay

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onRestore: () -> Unit,
    onNeedsAuth: ((String) -> Unit)? = null,
    isAuthenticated: Boolean = true,
    entryCount: Int = 0,
    totalWords: Int = 0,
    isFirstPaywallView: Boolean = true,
    annualPrice: String = "$19.99/year"
) {
    val handlePlanSelect: (String) -> Unit = { sku -> onSelectPlan(sku) }

    // Stagger visibility states
    var heroVisible by remember { mutableStateOf(false) }
    var featuresVisible by remember { mutableStateOf(false) }
    var pricingVisible by remember { mutableStateOf(false) }
    var ctaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        heroVisible = true
        delay(200)
        featuresVisible = true
        delay(200)
        pricingVisible = true
        delay(200)
        ctaVisible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = BottomSheetShape,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = DiarySpacing.screenHorizontal)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Hero section ────────────────────────────────────────
                Spacer(Modifier.height(DiarySpacing.xxl))

                AnimatedVisibility(
                    visible = heroVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Proactive Diary",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(DiarySpacing.xs))

                        Text(
                            text = "A private space for your thoughts.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(DiarySpacing.xl))

                // ── Feature showcase (3 glass cards) ────────────────────
                AnimatedVisibility(
                    visible = featuresVisible,
                    enter = fadeIn(tween(400))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
                    ) {
                        FeatureShowcaseCard(
                            icon = { Icon(Icons.Outlined.AllInclusive, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            title = "Unlimited writing",
                            description = "Photos, voice notes, templates, and more",
                            delayMs = 0
                        )
                        FeatureShowcaseCard(
                            icon = { Icon(Icons.Outlined.Psychology, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            title = "Weekly insights",
                            description = "Patterns and gentle suggestions from your journal",
                            delayMs = 100
                        )
                        FeatureShowcaseCard(
                            icon = { Icon(Icons.Outlined.Shield, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            title = "Privacy-first",
                            description = "Your data stays on your device. Always.",
                            delayMs = 200
                        )
                    }
                }

                Spacer(Modifier.height(DiarySpacing.lg))

                // ── Privacy badge ───────────────────────────────────────
                PrivacyBadge(text = "On-device only")

                Spacer(Modifier.height(DiarySpacing.xl))

                // ── Price presentation ──────────────────────────────────
                AnimatedVisibility(
                    visible = pricingVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 30 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$1.67",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(DiarySpacing.xxs))
                        Text(
                            text = "per month, billed annually at $19.99",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(DiarySpacing.lg))

                // ── Social proof ────────────────────────────────────────
                AnimatedVisibility(
                    visible = ctaVisible,
                    enter = fadeIn(tween(400))
                ) {
                    Text(
                        text = "Join thousands of daily writers",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(DiarySpacing.md))

                // ── CTA button ──────────────────────────────────────────
                AnimatedVisibility(
                    visible = ctaVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }
                ) {
                    PaywallCTAButton(
                        text = if (isFirstPaywallView) "Start 7-day free trial" else "Start your journey",
                        onClick = { handlePlanSelect(BillingService.ANNUAL_SKU) }
                    )
                }

                Spacer(Modifier.height(DiarySpacing.md))

                // ── Reassurance ─────────────────────────────────────────
                Text(
                    text = "Cancel anytime. Your entries are always yours.",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(DiarySpacing.sm))

                // ── Restore purchases ───────────────────────────────────
                TextButton(onClick = onRestore) {
                    Text(
                        text = "Restore Purchases",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(DiarySpacing.lg))
            }
        }
    }
}

@Composable
private fun FeatureShowcaseCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    delayMs: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.md)
            ) {
                icon()
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallCTAButton(
    text: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DiaryMotion.BUTTON_PRESS_SCALE else 1f,
        animationSpec = DiaryMotion.BouncySpring,
        label = "cta_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        shape = PillShape,
        color = MaterialTheme.colorScheme.onBackground
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}
