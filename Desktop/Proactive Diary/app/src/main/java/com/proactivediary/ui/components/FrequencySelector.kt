package com.proactivediary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.GoalFrequency

/**
 * Three segmented chips for selecting goal frequency: Daily / Weekly / Monthly.
 */
@Composable
fun FrequencySelector(
    selected: GoalFrequency,
    onSelected: (GoalFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = GoalFrequency.entries
    val shape = RoundedCornerShape(6.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { frequency ->
            val isSelected = frequency == selected
            val inkColor = MaterialTheme.colorScheme.onBackground
            val surfaceColor = MaterialTheme.colorScheme.surface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(shape)
                    .then(
                        if (isSelected) {
                            Modifier.background(inkColor, shape)
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = inkColor.copy(alpha = 0.4f),
                                shape = shape
                            )
                        }
                    )
                    .clickable { onSelected(frequency) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = frequency.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) surfaceColor else inkColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
