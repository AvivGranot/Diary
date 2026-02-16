package com.proactivediary.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.DynamicColors
import com.proactivediary.ui.theme.PlusJakartaSans
import java.io.File
import java.io.FileOutputStream

enum class ShareAspectRatio(val widthDp: Int, val heightDp: Int, val label: String) {
    SQUARE(360, 360, "Square"),
    STORY(360, 640, "Story"),
    WIDE(480, 270, "Wide")
}

enum class ShareCardStyle(val label: String) {
    SUNSET("Sunset"),
    OCEAN("Ocean"),
    NEON("Neon"),
    DARK("Dark"),
    PASTEL("Pastel")
}

data class ShareCardData(
    val excerpt: String,
    val title: String = "",
    val dateFormatted: String = "",
    val mood: String? = null,
    val colorKey: String = "indigo"
)

private fun gradientForStyle(style: ShareCardStyle): List<Color> {
    return when (style) {
        ShareCardStyle.SUNSET -> listOf(Color(0xFFF97316), Color(0xFFEC4899), Color(0xFF8B5CF6))
        ShareCardStyle.OCEAN -> listOf(Color(0xFF14B8A6), Color(0xFF3B82F6), Color(0xFF6366F1))
        ShareCardStyle.NEON -> listOf(Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF97316))
        ShareCardStyle.DARK -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4C1D95))
        ShareCardStyle.PASTEL -> listOf(Color(0xFFFDF2F8), Color(0xFFEDE9FE), Color(0xFFDBEAFE))
    }
}

private fun textColorForStyle(style: ShareCardStyle): Color {
    return if (style == ShareCardStyle.PASTEL) Color(0xFF1E293B) else Color.White
}

private fun secondaryTextForStyle(style: ShareCardStyle): Color {
    return if (style == ShareCardStyle.PASTEL) Color(0xFF64748B) else Color.White.copy(alpha = 0.7f)
}

@Composable
fun ShareCardPreview(
    data: ShareCardData,
    aspectRatio: ShareAspectRatio = ShareAspectRatio.SQUARE,
    style: ShareCardStyle = ShareCardStyle.SUNSET,
    modifier: Modifier = Modifier
) {
    val gradient = gradientForStyle(style)
    val textColor = textColorForStyle(style)
    val secondaryColor = secondaryTextForStyle(style)

    val moodEmoji = when (data.mood) {
        "great" -> "\uD83D\uDE04"
        "good" -> "\uD83D\uDE0A"
        "okay" -> "\uD83D\uDE10"
        "bad" -> "\uD83D\uDE1E"
        "awful" -> "\uD83D\uDE2D"
        else -> null
    }

    Box(
        modifier = modifier
            .width(aspectRatio.widthDp.dp)
            .height(aspectRatio.heightDp.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center)
        ) {
            if (moodEmoji != null) {
                Text(
                    text = moodEmoji,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = "\u201C",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.4f)
                )
            )

            Text(
                text = data.excerpt,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    lineHeight = 26.sp
                ),
                maxLines = if (aspectRatio == ShareAspectRatio.STORY) 14 else 6,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            if (data.dateFormatted.isNotBlank()) {
                Text(
                    text = data.dateFormatted,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = secondaryColor,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        Text(
            text = "proactive diary",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.35f),
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

data class StreakShareData(
    val streakCount: Int,
    val milestone: String,
    val mood: String? = null
)

@Composable
fun StreakCardPreview(
    data: StreakShareData,
    modifier: Modifier = Modifier
) {
    val gradient = data.mood?.let { DynamicColors.moodGradient(it) }
        ?: listOf(DiaryColors.ElectricIndigo, DiaryColors.NeonPink)

    Box(
        modifier = modifier
            .width(360.dp)
            .height(360.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83D\uDD25",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "${data.streakCount}",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "day streak",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = data.milestone,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f)
                ),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "proactive diary",
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.3f),
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

fun shareCardAsImage(context: Context, bitmap: Bitmap, shareType: String = "entry") {
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
        putExtra(Intent.EXTRA_TEXT, "Written in Proactive Diary \u2014 https://play.google.com/store/apps/details?id=com.proactivediary")
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share your entry"))
}
