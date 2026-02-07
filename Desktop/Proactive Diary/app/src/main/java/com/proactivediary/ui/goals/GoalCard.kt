package com.proactivediary.ui.goals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.components.parseStoredTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goal: GoalUiState,
    onCheckIn: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val parchmentColor = MaterialTheme.colorScheme.surface
    val (hour, minute) = parseStoredTime(goal.reminderTime)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(parchmentColor)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
            .padding(16.dp)
    ) {
        Column {
            // Title
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleLarge,
                color = inkColor
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle: frequency + time
            Text(
                text = "${goal.frequency.label} \u00B7 ${formatTime(hour, minute)}",
                style = MaterialTheme.typography.bodyMedium,
                color = pencilColor
            )

            Spacer(Modifier.height(12.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(pencilColor.copy(alpha = 0.15f))
                ) {
                    val fraction = (goal.progressPercent / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(inkColor)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${goal.progressPercent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = inkColor,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Streak + Check-in button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.streak > 0) "\uD83D\uDD25 ${goal.streak} day streak" else "No streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = pencilColor
                )

                // Check-in button
                if (goal.checkedInToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(inkColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = parchmentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Done",
                                style = MaterialTheme.typography.bodySmall,
                                color = parchmentColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, inkColor, RoundedCornerShape(4.dp))
                            .combinedClickable(onClick = onCheckIn)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Check In",
                            style = MaterialTheme.typography.bodySmall,
                            color = inkColor
                        )
                    }
                }
            }
        }
    }
}
