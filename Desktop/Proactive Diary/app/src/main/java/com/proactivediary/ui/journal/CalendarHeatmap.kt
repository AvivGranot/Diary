package com.proactivediary.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.InstrumentSerif
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun CalendarHeatmap(
    writingDays: Set<LocalDate>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    // Show last 12 weeks (84 days)
    val startDate = today.minusWeeks(11).with(DayOfWeek.MONDAY)
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt() + 1
    val weeks = (totalDays + 6) / 7

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.secondary
    // Blue shades for writing intensity
    val emptyColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    val filledColor = Color(0xFF3B82F6)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Writing Activity",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 16.sp,
                    color = inkColor
                )
            )

            Spacer(Modifier.height(12.dp))

            // Day labels (Mon, Wed, Fri)
            Row {
                // Label column
                Column(
                    modifier = Modifier.width(24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("", "M", "", "W", "", "F", "").forEach { label ->
                        Box(
                            modifier = Modifier.size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    style = TextStyle(fontSize = 8.sp, color = pencilColor)
                                )
                            }
                        }
                    }
                }

                // Heatmap grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (week in 0 until weeks) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (dayOfWeek in 0..6) {
                                val date = startDate.plusWeeks(week.toLong()).plusDays(dayOfWeek.toLong())
                                val isWritten = date in writingDays
                                val isFuture = date.isAfter(today)

                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                isFuture -> Color.Transparent
                                                isWritten -> filledColor.copy(alpha = 0.7f)
                                                else -> emptyColor
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Less", style = TextStyle(fontSize = 9.sp, color = pencilColor))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(emptyColor))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(filledColor.copy(alpha = 0.3f)))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(filledColor.copy(alpha = 0.7f)))
                Text("More", style = TextStyle(fontSize = 9.sp, color = pencilColor))
            }
        }
    }
}
