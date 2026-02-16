package com.proactivediary.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.quotes.components.LeaderboardHeader
import com.proactivediary.ui.quotes.components.QuoteCard
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun QuotesScreen(
    onQuoteClick: (String) -> Unit,
    onSendNote: () -> Unit = {},
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            Box(modifier = Modifier.padding(bottom = 56.dp)) {
                FloatingActionButton(
                    onClick = { viewModel.showComposeSheet() },
                    containerColor = DiaryColors.WineRed
                ) {
                    Icon(Icons.Default.Add, "Write a quote", tint = Color.White)
                }
            }
        },
        containerColor = DiaryColors.Paper
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Text(
                text = "Quotes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = DiaryColors.Ink,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Send a note CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.5.dp, DiaryColors.Pencil.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .background(DiaryColors.Parchment)
                    .clickable(onClick = onSendNote)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = "Send a note",
                    tint = DiaryColors.WineRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Send an anonymous note to a friend",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = DiaryColors.Ink
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuotesTab.entries.forEach { tab ->
                    val isSelected = state.selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) DiaryColors.Ink
                                else DiaryColors.Parchment
                            )
                            .clickable { viewModel.selectTab(tab) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (tab) {
                                QuotesTab.TRENDING -> "Trending"
                                QuotesTab.NEW -> "New"
                                QuotesTab.MY_QUOTES -> "My Quotes"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else DiaryColors.Ink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CTA prompt
            Text(
                text = "Share your wisdom \u2014 inspire someone today",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = DiaryColors.Pencil,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            val currentQuotes = when (state.selectedTab) {
                QuotesTab.TRENDING -> state.trendingQuotes
                QuotesTab.NEW -> state.newQuotes
                QuotesTab.MY_QUOTES -> state.myQuotes
            }

            if (currentQuotes.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 120.dp)
                    ) {
                        Text(
                            text = "The stage is yours",
                            style = MaterialTheme.typography.titleMedium,
                            color = DiaryColors.Ink
                        )
                        Text(
                            text = "Drop your first quote.\n25 words that could inspire thousands.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = DiaryColors.Pencil,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Leaderboard header (only on Trending tab)
                    if (state.selectedTab == QuotesTab.TRENDING && state.trendingQuotes.size >= 3) {
                        item(key = "leaderboard") {
                            LeaderboardHeader(topQuotes = state.trendingQuotes.take(3))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    items(currentQuotes, key = { it.id }) { quote ->
                        QuoteCard(
                            quote = quote,
                            isLiked = state.likedQuoteIds.contains(quote.id),
                            onLike = { viewModel.toggleLike(quote.id) },
                            onClick = { onQuoteClick(quote.id) }
                        )
                    }
                }
            }
        }
    }

    // Compose quote bottom sheet
    if (state.composeSheetVisible) {
        ComposeQuoteSheet(
            content = state.composeContent,
            wordCount = state.composeWordCount,
            isSubmitting = state.isSubmitting,
            error = state.submitError,
            onContentChange = { viewModel.updateComposeContent(it) },
            onSubmit = { viewModel.submitQuote() },
            onDismiss = { viewModel.hideComposeSheet() }
        )
    }
}
