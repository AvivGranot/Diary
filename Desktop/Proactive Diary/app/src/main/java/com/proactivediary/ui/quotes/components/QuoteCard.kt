package com.proactivediary.ui.quotes.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.data.social.Quote
import com.proactivediary.ui.theme.DiaryColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuoteCard(
    quote: Quote,
    isLiked: Boolean,
    onLike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val likeColor by animateColorAsState(
        targetValue = if (isLiked) DiaryColors.CoralRed else DiaryColors.Pencil,
        label = "likeColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DiaryColors.Parchment)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        // Quote content
        Text(
            text = "\u201C${quote.content}\u201D",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            fontStyle = FontStyle.Italic,
            color = DiaryColors.Ink,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Author + avatar + timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AuthorAvatar(
                    photoUrl = quote.authorPhotoUrl,
                    authorName = quote.authorName,
                    size = 24
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\u2014 ${quote.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DiaryColors.Pencil
                )
            }

            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            Text(
                text = dateFormat.format(Date(quote.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = DiaryColors.Pencil.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = likeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "${quote.likeCount}",
                style = MaterialTheme.typography.bodySmall,
                color = DiaryColors.Pencil
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = DiaryColors.Pencil,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${quote.commentCount}",
                style = MaterialTheme.typography.bodySmall,
                color = DiaryColors.Pencil
            )
        }
    }
}
