package com.proactivediary.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond

data class JournalInsights(
    val totalEntries: Int = 0,
    val totalWords: Int = 0,
    val averageWordsPerEntry: Int = 0,
    val currentStreak: Int = 0,
    val mostActiveDay: String? = null // e.g., "Tuesdays"
)

@Composable
fun InsightsCard(
    insights: JournalInsights,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.secondary

    if (insights.totalEntries == 0) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Writing",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 18.sp,
                        color = inkColor
                    )
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = pencilColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Summary line always visible
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${insights.totalEntries} entries \u00B7 ${insights.totalWords} words",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = pencilColor
                )
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))

                    // Stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Avg words", value = "${insights.averageWordsPerEntry}")
                        if (insights.currentStreak > 0) {
                            StatItem(label = "Practice", value = "Day ${insights.currentStreak}")
                        }
                    }

                    if (insights.mostActiveDay != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "You write most on ${insights.mostActiveDay}.",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = pencilColor
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
}
