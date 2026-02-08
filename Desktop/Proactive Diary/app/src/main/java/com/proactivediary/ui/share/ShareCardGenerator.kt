package com.proactivediary.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.CormorantGaramond
import java.io.File
import java.io.FileOutputStream

/** Aspect ratio preset for share cards */
enum class ShareAspectRatio(val widthDp: Int, val heightDp: Int, val label: String) {
    SQUARE(360, 360, "Square"),
    STORY(360, 640, "Story"),   // 9:16 for Instagram Stories
    WIDE(480, 270, "Wide")      // 16:9 for Twitter
}

data class ShareCardData(
    val excerpt: String,
    val title: String = "",
    val dateFormatted: String = "",
    val colorKey: String = "cream",
    val mood: String? = null
)

@Composable
fun ShareCardPreview(
    data: ShareCardData,
    aspectRatio: ShareAspectRatio = ShareAspectRatio.SQUARE,
    modifier: Modifier = Modifier
) {
    val bgColor = DiaryThemeConfig.colorForKey(data.colorKey)
    val textColor = DiaryThemeConfig.textColorFor(data.colorKey)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(data.colorKey)

    Surface(
        modifier = modifier
            .width(aspectRatio.widthDp.dp)
            .height(aspectRatio.heightDp.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = bgColor,
        shadowElevation = 4.dp
    ) {
        Box {
            Column(
                modifier = Modifier.padding(32.dp)
            ) {
                // Opening quotation mark
                Text(
                    text = "\u201C",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 48.sp,
                        color = textColor.copy(alpha = 0.3f)
                    )
                )

                // Excerpt
                Text(
                    text = data.excerpt,
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Italic,
                        color = textColor,
                        lineHeight = 28.sp
                    ),
                    maxLines = if (aspectRatio == ShareAspectRatio.STORY) 14 else 8,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(24.dp))

                // Divider line
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(secondaryTextColor.copy(alpha = 0.3f))
                )

                Spacer(Modifier.height(12.dp))

                // Date
                if (data.dateFormatted.isNotBlank()) {
                    Text(
                        text = data.dateFormatted,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // Whispered watermark — bottom-right, 8pt Cormorant Garamond
            Text(
                text = "Written in Proactive Diary",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 8.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor.copy(alpha = 0.35f),
                    letterSpacing = 0.2.sp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            )
        }
    }
}

data class StreakShareData(
    val streakCount: Int,
    val milestone: String,
    val colorKey: String = "cream"
)


@Composable
fun StreakCardPreview(
    data: StreakShareData,
    modifier: Modifier = Modifier
) {
    val bgColor = DiaryThemeConfig.colorForKey(data.colorKey)
    val textColor = DiaryThemeConfig.textColorFor(data.colorKey)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(data.colorKey)

    Surface(
        modifier = modifier
            .width(360.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = bgColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Practice day number
            Text(
                text = "${data.streakCount}.",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 56.sp,
                    color = textColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Practice count
            Text(
                text = "Day ${data.streakCount} of my writing practice",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 28.sp,
                    color = textColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Milestone subtitle
            Text(
                text = data.milestone,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Divider line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .background(secondaryTextColor.copy(alpha = 0.3f))
            )

            Spacer(Modifier.height(24.dp))

            // Whispered watermark — bottom-right, 8pt
            Text(
                text = "Written in Proactive Diary",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 8.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor.copy(alpha = 0.35f),
                    letterSpacing = 0.2.sp
                )
            )
        }
    }
}

fun shareCardAsImage(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "share_cards")
    cachePath.mkdirs()
    val file = File(cachePath, "diary_card.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share your entry"))
}
