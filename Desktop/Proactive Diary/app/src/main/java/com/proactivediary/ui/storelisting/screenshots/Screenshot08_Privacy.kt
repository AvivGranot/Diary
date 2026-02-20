package com.proactivediary.ui.storelisting.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
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
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.ProactiveDiaryTheme

@Composable
fun Screenshot08Privacy() {
    ScreenshotFrame(headline = "Your words are yours alone") {
        PrivacyContent()
    }
}

@Composable
private fun PrivacyContent() {
    val inkColor = Color(0xFFFFFFFF)
    val pencilColor = Color(0xFF888888)

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
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Privacy",
                modifier = Modifier.size(48.dp),
                tint = inkColor
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "All your data stays\non this device",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 24.sp,
                    color = inkColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "We never collect, transmit, or\nanalyze what you write",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = pencilColor
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No account required",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = pencilColor.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = "Proactive Diary",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 20.sp,
                    color = inkColor
                )
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun Screenshot08PrivacyPreview() {
    ProactiveDiaryTheme {
        Screenshot08Privacy()
    }
}
