package com.proactivediary.ui.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.data.social.Quote
import com.proactivediary.data.social.QuotesRepository
import com.proactivediary.data.social.UserProfileRepository
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun QuotesPreviewScreen(
    onContinue: () -> Unit,
    quotesRepository: QuotesRepository,
    userProfileRepository: UserProfileRepository
) {
    var topQuotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var quoteCount by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val result = quotesRepository.getLeaderboard("weekly", 5)
        result.onSuccess { topQuotes = it }

        val (_, quotes) = userProfileRepository.getGlobalCounters()
        quoteCount = quotes
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Top Quotes\nThis Week",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = DiaryColors.Ink,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Can you beat them?",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = DiaryColors.Pencil
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (topQuotes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topQuotes, key = { it.id }) { quote ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DiaryColors.Parchment)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "\u201C${quote.content}\u201D",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic,
                                    color = DiaryColors.Ink
                                )
                                Text(
                                    text = "\u2014 ${quote.authorName}  \u00B7  ${quote.likeCount} \u2764",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DiaryColors.Pencil,
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
                            color = DiaryColors.Pencil,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (quoteCount > 0) {
                Text(
                    text = "$quoteCount quotes shared this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiaryColors.Pencil,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiaryColors.ElectricIndigo
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "I\u2019m In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
