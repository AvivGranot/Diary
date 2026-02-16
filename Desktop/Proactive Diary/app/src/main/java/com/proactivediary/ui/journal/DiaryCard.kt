package com.proactivediary.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.DynamicColors
import com.proactivediary.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiaryCardData(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val wordCount: Int,
    val createdAt: Long,
    val imageCount: Int = 0,
    val mood: String? = null
)

@Composable
fun DiaryCard(
    data: DiaryCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodAccent = data.mood?.let { DynamicColors.moodAccent(it) }
        ?: DiaryColors.Pencil

    val displayTitle = data.title.ifBlank {
        data.content.lines().firstOrNull()?.take(60) ?: ""
    }

    val contentPreview = if (data.title.isNotBlank()) {
        data.content.trimStart().take(200)
    } else {
        val lines = data.content.lines()
        if (lines.size > 1) {
            lines.drop(1).joinToString("\n").trimStart().take(200)
        } else {
            ""
        }
    }

    val dateText = try {
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        sdf.format(Date(data.createdAt))
    } catch (_: Exception) { "" }

    val moodEmoji = when (data.mood) {
        "great" -> "\uD83D\uDE04"
        "good" -> "\uD83D\uDE0A"
        "okay" -> "\uD83D\uDE10"
        "bad" -> "\uD83D\uDE1E"
        "awful" -> "\uD83D\uDE2D"
        else -> null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .height(IntrinsicSize.Min)
        ) {
            // Mood accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = data.mood?.let { DynamicColors.moodGradient(it) }
                                ?: listOf(moodAccent, moodAccent.copy(alpha = 0.5f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Title row with mood emoji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (displayTitle.isNotBlank()) {
                        Text(
                            text = displayTitle,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (moodEmoji != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = moodEmoji,
                            fontSize = 16.sp
                        )
                    }
                }

                // Tags
                if (data.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.tags.joinToString("  ") { "#$it" },
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = moodAccent.copy(alpha = 0.7f)
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
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            lineHeight = 19.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dateText.isNotBlank()) {
                        Text(
                            text = dateText,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }

                    if (data.imageCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "${data.imageCount}",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (data.wordCount >= 50) {
                        Text(
                            text = "${data.wordCount} words",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}
