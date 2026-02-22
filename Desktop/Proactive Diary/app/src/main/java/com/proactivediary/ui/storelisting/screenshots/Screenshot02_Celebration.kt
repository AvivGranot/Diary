package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.storelisting.ScreenshotFrame
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.ProactiveDiaryTheme

@Composable
fun Screenshot02Celebration() {
    ScreenshotFrame(headline = "Celebrate your consistency") {
        CelebrationContent()
    }
}

@Composable
private fun CelebrationContent() {
    val inkColor = Color(0xFFFFFFFF)
    val pencilColor = Color(0xFF888888)
    val parchmentColor = Color(0xFF111111)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Text(
                text = "30.",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 56.sp,
                    color = inkColor
                )
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "One month",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 32.sp,
                    color = inkColor
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "A month of writing.\nThis is who you are now.",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    color = pencilColor
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = inkColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SHARE",
                    style = TextStyle(
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = parchmentColor
                    )
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot02CelebrationPreview() {
    ProactiveDiaryTheme {
        Screenshot02Celebration()
    }
}
