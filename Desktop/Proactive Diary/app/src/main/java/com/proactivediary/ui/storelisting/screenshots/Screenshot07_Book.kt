package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.ProactiveDiaryTheme

@Composable
fun Screenshot07Book() {
    ScreenshotFrame(headline = "Your year, one beautiful book") {
        BookContent()
    }
}

@Composable
private fun BookContent() {
    val inkColor = Color(0xFFFFFFFF)
    val pencilColor = Color(0xFF888888)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Year selector
        Text(
            text = "2026",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 32.sp,
                color = inkColor
            )
        )

        Spacer(Modifier.height(8.dp))

        // Stats
        Text(
            text = "156 entries \u00B7 45,200 words",
            style = TextStyle(
                fontSize = 13.sp,
                color = pencilColor
            )
        )

        Spacer(Modifier.height(32.dp))

        // Mock book cover
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(260.dp)
                .shadow(6.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "A Year of",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = inkColor
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Writing",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 28.sp,
                        color = inkColor
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Decorative line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(pencilColor.copy(alpha = 0.3f))
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "2026",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = pencilColor
                    )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Generate button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(inkColor)
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "GENERATE BOOK",
                style = TextStyle(
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF111111)
                )
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot07BookPreview() {
    ProactiveDiaryTheme {
        Screenshot07Book()
    }
}
