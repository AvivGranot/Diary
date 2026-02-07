package com.proactivediary.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.Mood
import com.proactivediary.ui.theme.CormorantGaramond

data class DiaryCardData(
    val id: String,
    val title: String,
    val content: String,
    val mood: Mood?,
    val tags: List<String>,
    val wordCount: Int,
    val createdAt: Long
)

@Composable
fun DiaryCard(
    data: DiaryCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parchment = Color(0xFFFAF9F5)
    val inkColor = Color(0xFF313131)
    val pencilColor = Color(0xFF585858)

    val displayTitle = data.title.ifBlank {
        data.content.lines().firstOrNull()?.take(60) ?: ""
    }

    val contentPreview = if (data.title.isNotBlank()) {
        data.content.take(200)
    } else {
        // Skip the first line since it's used as title
        val lines = data.content.lines()
        if (lines.size > 1) {
            lines.drop(1).joinToString("\n").take(200).trim()
        } else {
            ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(parchment)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Title / first line
        if (displayTitle.isNotBlank()) {
            Text(
                text = displayTitle,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    color = inkColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Content preview
        if (contentPreview.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = contentPreview,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = pencilColor,
                    lineHeight = 20.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom row: mood + tags + word count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mood circle
            if (data.mood != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(data.mood.color)
                )
            }

            // Tags
            if (data.tags.isNotEmpty()) {
                Text(
                    text = data.tags.joinToString(", ") { "#$it" },
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = pencilColor.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Word count
            Text(
                text = "${data.wordCount} words",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = pencilColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}
