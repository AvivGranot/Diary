package com.proactivediary.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuotesRepository
import com.proactivediary.data.social.UserProfileRepository

@Composable
fun QuotesPreviewScreen(
    onContinue: () -> Unit,
    analyticsService: AnalyticsService,
    quotesRepository: QuotesRepository,
    userProfileRepository: UserProfileRepository
) {
    var topQuotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var quoteCount by remember { mutableStateOf(0L) }
    val screenStartTime = remember { System.currentTimeMillis() }

    LaunchedEffect(Unit) {
        analyticsService.logOnboardingQuotesShown()
        try {
            val result = quotesRepository.getLeaderboard("weekly", 5)
            result.onSuccess { topQuotes = it }
        } catch (_: Exception) { /* Continue — button works regardless */ }

        try {
            val (_, quotes) = userProfileRepository.getGlobalCounters()
            quoteCount = quotes
        } catch (_: Exception) { /* Continue — button works regardless */ }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(OnboardingHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Top Quotes\nThis Week",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Can you beat them?",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (topQuotes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topQuotes, key = { it.id }) { quote ->
                        // White card with hairline border on linen bg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "\u201C${quote.content}\u201D",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "\u2014 ${quote.authorName}  \u00B7  ${quote.likeCount} \u2764",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No quotes yet.\nBe the one everyone quotes.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (quoteCount > 0) {
                Text(
                    text = "$quoteCount quotes shared this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Ink primary button, 48dp
            OnboardingPrimaryButton(
                text = "I\u2019m In",
                onClick = {
                    analyticsService.logOnboardingQuotesComplete(
                        System.currentTimeMillis() - screenStartTime
                    )
                    onContinue()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
