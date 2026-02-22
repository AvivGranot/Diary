package com.proactivediary.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.InstrumentSerif

data class WeeklyDigest(
    val entryCount: Int = 0,
    val totalWords: Int = 0,
    val isVisible: Boolean = false
)

@Composable
fun WeeklyDigestCard(
    digest: WeeklyDigest,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!digest.isVisible || digest.entryCount == 0) return

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Week of Writing",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 18.sp,
                        color = inkColor
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = pencilColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = buildString {
                    append("This week: ${digest.entryCount} ")
                    append(if (digest.entryCount == 1) "entry" else "entries")
                    append(", ${digest.totalWords} words.")
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = pencilColor,
                    lineHeight = 20.sp
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when {
                    digest.entryCount >= 5 -> "Incredible week of practice."
                    digest.entryCount >= 3 -> "Your practice is growing."
                    digest.entryCount >= 1 -> "Every word counts."
                    else -> ""
                },
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 14.sp,
                    color = inkColor
                )
            )
        }
    }
}
