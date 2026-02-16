package com.proactivediary.ui.quotes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.data.social.Quote
import com.proactivediary.ui.theme.DiaryColors

private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)

@Composable
fun LeaderboardHeader(
    topQuotes: List<Quote>,
    modifier: Modifier = Modifier
) {
    if (topQuotes.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiaryColors.ElectricIndigo.copy(alpha = 0.08f))
            .padding(20.dp)
    ) {
        Text(
            text = "Top Quotes This Week",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = DiaryColors.Ink,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        topQuotes.take(3).forEachIndexed { index, quote ->
            val (rankColor, rankLabel) = when (index) {
                0 -> Gold to "1st"
                1 -> Silver to "2nd"
                2 -> Bronze to "3rd"
                else -> DiaryColors.Pencil to "${index + 1}th"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(rankColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quote content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "\u201C${quote.content}\u201D",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = DiaryColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "\u2014 ${quote.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DiaryColors.Pencil,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Like count
                Text(
                    text = "${quote.likeCount} \u2764",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiaryColors.Pencil
                )
            }
        }
    }
}
