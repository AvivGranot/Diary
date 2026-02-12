package com.proactivediary.ui.journal

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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond
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
    val imageCount: Int = 0
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
        val lines = data.content.lines()
        if (lines.size > 1) {
            lines.drop(1).joinToString("\n").take(200).trim()
        } else {
            ""
        }
    }

    val dateText = try {
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        sdf.format(Date(data.createdAt))
    } catch (_: Exception) { "" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        color = parchment
    ) {
        Column(
            modifier = Modifier
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

            // Bottom row: date + tags + image count + word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date
                if (dateText.isNotBlank()) {
                    Text(
                        text = dateText,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = pencilColor.copy(alpha = 0.5f)
                        )
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

                // Image count indicator
                if (data.imageCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = pencilColor.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${data.imageCount}",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = pencilColor.copy(alpha = 0.5f)
                            )
                        )
                    }
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
}
