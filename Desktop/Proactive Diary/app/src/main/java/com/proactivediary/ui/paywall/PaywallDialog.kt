package com.proactivediary.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.proactivediary.ui.theme.CormorantGaramond

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
    monthlyPrice: String = "$5/month",
    annualPrice: String = "$30/year"
) {
    // Remove auth gate: always go directly to purchase
    val handlePlanSelect: (String) -> Unit = { sku -> onSelectPlan(sku) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header — personalized based on engagement
                Text(
                    text = if (entryCount >= 10) {
                        "You\u2019ve built a writing practice.\nKeep it alive."
                    } else if (entryCount > 0) {
                        "You\u2019ve written $entryCount entries.\n$totalWords words so far."
                    } else {
                        "A private daily writing practice."
                    },
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Subheader
                Text(
                    text = "Your words deserve a home.",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Feature list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureRow("Unlimited writing & photos")
                    FeatureRow("AI-powered writing insights")
                    FeatureRow("Templates & guided programs")
                    FeatureRow("Voice notes & advanced export")
                    FeatureRow("Privacy-first \u2014 your data stays on device")
                }

                Spacer(Modifier.height(24.dp))

                // Free trial CTA for first-time paywall viewers
                if (isFirstPaywallView) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { handlePlanSelect(BillingService.ANNUAL_SKU) }),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Start 7-day free trial",
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Try everything. Then decide.",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Monthly plan card
                PlanCard(
                    title = "Monthly",
                    price = monthlyPrice,
                    onClick = { handlePlanSelect(BillingService.MONTHLY_SKU) }
                )

                Spacer(Modifier.height(12.dp))

                // Annual plan card
                PlanCard(
                    title = "Annual",
                    price = annualPrice,
                    badge = "Save 50%",
                    bestValue = true,
                    onClick = { handlePlanSelect(BillingService.ANNUAL_SKU) }
                )

                Spacer(Modifier.height(12.dp))

                // Urgency line
                Text(
                    text = "Your entries are safe. Upgrade to keep your practice going.",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Restore purchases
                TextButton(onClick = onRestore) {
                    Text(
                        text = "Restore Purchases",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Privacy note
                Text(
                    text = "Your data stays on your device.\nWe never see what you write.",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "\u2713",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    badge: String? = null,
    bestValue: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (bestValue) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
                Text(
                    text = price,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            if (bestValue) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "BEST VALUE",
                    style = TextStyle(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}
