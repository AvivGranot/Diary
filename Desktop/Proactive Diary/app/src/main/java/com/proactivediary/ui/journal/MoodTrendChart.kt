package com.proactivediary.ui.journal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.Mood
import com.proactivediary.ui.theme.CormorantGaramond
import java.time.LocalDate

data class MoodDataPoint(
    val date: LocalDate,
    val mood: Mood
)

@Composable
fun MoodTrendChart(
    dataPoints: List<MoodDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.size < 2) return

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.secondary
    val lineColor = Color(0xFF5B8C5A)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mood Trend",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 16.sp,
                    color = inkColor
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Last 30 days",
                style = TextStyle(fontSize = 11.sp, color = pencilColor)
            )

            Spacer(Modifier.height(12.dp))

            // Mood labels on the right
            Row(modifier = Modifier.fillMaxWidth()) {
                // Chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val padding = 8f

                        // Map mood to Y value (GREAT=top, TERRIBLE=bottom)
                        fun moodToY(mood: Mood): Float {
                            val value = when (mood) {
                                Mood.GREAT -> 1.0f
                                Mood.GOOD -> 0.75f
                                Mood.NEUTRAL -> 0.5f
                                Mood.BAD -> 0.25f
                                Mood.TERRIBLE -> 0.0f
                            }
                            return height - padding - (value * (height - 2 * padding))
                        }

                        if (dataPoints.size >= 2) {
                            val path = Path()
                            val xStep = (width - 2 * padding) / (dataPoints.size - 1).coerceAtLeast(1)

                            dataPoints.forEachIndexed { index, point ->
                                val x = padding + index * xStep
                                val y = moodToY(point.mood)

                                if (index == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }

                                // Draw dot at each point
                                drawCircle(
                                    color = point.mood.color,
                                    radius = 3f,
                                    center = Offset(x, y)
                                )
                            }

                            drawPath(
                                path = path,
                                color = lineColor.copy(alpha = 0.6f),
                                style = Stroke(width = 2f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // Y-axis labels
                Column(
                    modifier = Modifier
                        .width(16.dp)
                        .height(80.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("\uD83D\uDE0A", fontSize = 10.sp) // happy face
                    Text("\uD83D\uDE10", fontSize = 10.sp) // neutral face
                    Text("\uD83D\uDE1E", fontSize = 10.sp) // sad face
                }
            }
        }
    }
}
