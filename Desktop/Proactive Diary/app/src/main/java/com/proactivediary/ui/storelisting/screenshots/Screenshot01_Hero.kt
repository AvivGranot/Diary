package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.ProactiveDiaryTheme

@Composable
fun Screenshot01Hero() {
    ScreenshotFrame(headline = "Your private writing sanctuary") {
        HeroContent()
    }
}

@Composable
private fun HeroContent() {
    val inkColor = DiaryColors.Ink
    val pencilColor = DiaryColors.Pencil
    val lineColor = DiaryColors.Pencil.copy(alpha = 0.12f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Date header
        Text(
            text = "Wednesday, February 11, 2026",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = pencilColor
            )
        )

        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            text = "Morning reflections",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 24.sp,
                color = inkColor
            )
        )

        Spacer(Modifier.height(20.dp))

        // Journal lines with text
        val lines = listOf(
            "There\u2019s something about early mornings that clears",
            "the mind. I woke before the alarm today and sat",
            "with my coffee watching the light come in through",
            "the kitchen window. I keep thinking about what"
        )

        lines.forEach { line ->
            Column {
                Text(
                    text = line,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = inkColor,
                        lineHeight = 28.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(lineColor)
                )
            }
        }

        // Empty lined area
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.BottomCenter)
                        .background(lineColor)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Bottom bar: saved + word count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Saved",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF5B8C5A)
                    )
                )
                Text(
                    text = "42 words",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = pencilColor.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot01HeroPreview() {
    ProactiveDiaryTheme {
        Screenshot01Hero()
    }
}
