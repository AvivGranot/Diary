package com.proactivediary.ui.storelisting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun ScreenshotFrame(
    headline: String,
    subheadline: String = "",
    backgroundColor: Color = Color(0xFF000000),
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top band (~20%) — headline + subheadline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = headline,
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 28.sp,
                        color = Color(0xFFFFFFFF)
                    ),
                    textAlign = TextAlign.Center
                )
                if (subheadline.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subheadline,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF888888)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // App content area (~80%) — phone mock with rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.15f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}
