package com.proactivediary.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun VoiceRecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    var durationSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            durationSeconds = 0
            while (true) {
                delay(1000)
                durationSeconds++
            }
        } else {
            durationSeconds = 0
        }
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isRecording) Color(0xFFE57373).copy(alpha = 0.2f) else Color.Transparent)
            .clickable {
                if (isRecording) onStopRecording() else onStartRecording()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = if (isRecording) "Stop recording" else "Start recording",
            tint = if (isRecording) Color(0xFFD32F2F) else secondaryTextColor.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
        if (isRecording) {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            Text(
                text = String.format("%d:%02d", minutes, seconds),
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F)
                )
            )
        }
    }
}
