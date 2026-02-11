package com.proactivediary.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.proactivediary.domain.model.GoalFrequency
import com.proactivediary.notifications.NotificationService
import com.proactivediary.ui.components.DaySelector
import com.proactivediary.ui.components.FrequencySelector
import com.proactivediary.ui.components.WheelTimePicker
import com.proactivediary.ui.components.formatTimeForStorage
import com.proactivediary.ui.components.parseStoredTime

@Composable
fun AddGoalDialog(
    editingGoal: GoalUiState? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, frequency: GoalFrequency, time: String, days: String) -> Unit
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val parchmentColor = MaterialTheme.colorScheme.surface

    var title by remember { mutableStateOf(editingGoal?.title ?: "") }
    var frequency by remember { mutableStateOf(editingGoal?.frequency ?: GoalFrequency.DAILY) }

    val initialTime = if (editingGoal != null) {
        parseStoredTime(editingGoal.reminderTime)
    } else {
        Pair(6, 0)
    }
    var hour by remember { mutableIntStateOf(initialTime.first) }
    var minute by remember { mutableIntStateOf(initialTime.second) }

    val initialDays = if (editingGoal != null) {
        NotificationService.parseDays(editingGoal.reminderDays).toSet()
    } else {
        (0..6).toSet()
    }
    var selectedDays by remember { mutableStateOf(initialDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = parchmentColor,
        title = {
            Text(
                text = if (editingGoal != null) "Edit Goal" else "New Goal",
                style = MaterialTheme.typography.headlineLarge,
                color = inkColor
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title input
                Text(
                    text = "Goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = pencilColor
                )
                Spacer(Modifier.height(4.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = inkColor),
                    cursorBrush = SolidColor(inkColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Column {
                            if (title.isEmpty()) {
                                Text(
                                    "What's your goal?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = pencilColor.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = pencilColor.copy(alpha = 0.2f))
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Frequency
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall,
                    color = pencilColor
                )
                Spacer(Modifier.height(8.dp))
                FrequencySelector(
                    selected = frequency,
                    onSelected = { frequency = it }
                )

                Spacer(Modifier.height(16.dp))

                // Time
                Text(
                    text = "Remind at",
                    style = MaterialTheme.typography.bodySmall,
                    color = pencilColor
                )
                Spacer(Modifier.height(4.dp))
                WheelTimePicker(
                    hour = hour,
                    minute = minute,
                    onTimeChanged = { h, m -> hour = h; minute = m }
                )

                Spacer(Modifier.height(16.dp))

                // Days
                Text(
                    text = "Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = pencilColor
                )
                Spacer(Modifier.height(8.dp))
                DaySelector(
                    selectedDays = selectedDays,
                    onDayToggled = { day ->
                        selectedDays = selectedDays.toMutableSet().apply {
                            if (contains(day)) remove(day) else add(day)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val daysJson = "[${selectedDays.sorted().joinToString(",")}]"
                        val timeStr = formatTimeForStorage(hour, minute)
                        onSave(title, frequency, timeStr, daysJson)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save", color = if (title.isNotBlank()) inkColor else pencilColor.copy(alpha = 0.3f))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = pencilColor)
            }
        }
    )
}
