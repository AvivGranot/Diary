package com.proactivediary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.DiaryColors

/**
 * Days of the week selector with circle toggle chips.
 * @param selectedDays Set of day indices (0=Monday, 1=Tuesday, ..., 6=Sunday)
 * @param onDayToggled Called when a day is toggled
 */
@Composable
fun DaySelector(
    selectedDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayLabels.forEachIndexed { index, label ->
            val isSelected = selectedDays.contains(index)
            val inkColor = MaterialTheme.colorScheme.onBackground
            val surfaceColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(inkColor, CircleShape)
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = inkColor.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                        }
                    )
                    .clickable { onDayToggled(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) surfaceColor else inkColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
