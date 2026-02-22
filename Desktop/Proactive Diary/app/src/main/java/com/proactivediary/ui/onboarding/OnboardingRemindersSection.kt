package com.proactivediary.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.components.WheelTimePicker
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.theme.InstrumentSerif

@Composable
fun OnboardingRemindersSection(
    reminders: List<ReminderInput>,
    onReminderChanged: (Int, ReminderInput) -> Unit,
    onReminderAdded: () -> Unit,
    onReminderRemoved: (Int) -> Unit,
    canAddMore: Boolean,
    inkColor: Color,
    pencilColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        reminders.forEachIndexed { index, reminder ->
            ReminderRow(
                reminder = reminder,
                onChanged = { onReminderChanged(index, it) },
                onRemove = if (reminders.size > 1) {
                    { onReminderRemoved(index) }
                } else null,
                isPreset = index < 2,
                inkColor = inkColor,
                pencilColor = pencilColor
            )
            if (index < reminders.lastIndex) {
                Spacer(Modifier.height(8.dp))
            }
        }

        if (canAddMore) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "+ Add custom time",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = inkColor,
                modifier = Modifier.clickable { onReminderAdded() }
            )
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: ReminderInput,
    onChanged: (ReminderInput) -> Unit,
    onRemove: (() -> Unit)?,
    isPreset: Boolean,
    inkColor: Color,
    pencilColor: Color
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    var showTimePicker by remember { mutableStateOf(false) }
    var pickerHour by remember { mutableIntStateOf(reminder.hour) }
    var pickerMinute by remember { mutableIntStateOf(reminder.minute) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (reminder.enabled) {
                        Modifier.background(inkColor, RoundedCornerShape(4.dp))
                    } else {
                        Modifier.border(1.dp, pencilColor, RoundedCornerShape(4.dp))
                    }
                )
                .clickable { onChanged(reminder.copy(enabled = !reminder.enabled)) },
            contentAlignment = Alignment.Center
        ) {
            if (reminder.enabled) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Reminder enabled",
                    tint = surfaceColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Label
        if (isPreset) {
            Text(
                text = reminder.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (reminder.enabled) inkColor else pencilColor.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
        } else {
            BasicTextField(
                value = reminder.label,
                onValueChange = { onChanged(reminder.copy(label = it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (reminder.enabled) inkColor else pencilColor.copy(alpha = 0.5f)
                ),
                cursorBrush = SolidColor(inkColor),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Time — opens wheel picker dialog
        Text(
            text = formatTime(reminder.hour, reminder.minute),
            style = MaterialTheme.typography.bodyMedium,
            color = if (reminder.enabled) inkColor else pencilColor.copy(alpha = 0.5f),
            modifier = Modifier.clickable {
                pickerHour = reminder.hour
                pickerMinute = reminder.minute
                showTimePicker = true
            }
        )

        // Remove button for custom reminders
        if (onRemove != null && !isPreset) {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = pencilColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    // Wheel time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Set Time",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 20.sp,
                        color = inkColor
                    )
                )
            },
            text = {
                WheelTimePicker(
                    hour = pickerHour,
                    minute = pickerMinute,
                    onTimeChanged = { h, m ->
                        pickerHour = h
                        pickerMinute = m
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onChanged(reminder.copy(hour = pickerHour, minute = pickerMinute))
                    showTimePicker = false
                }) {
                    Text("Done", color = inkColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = pencilColor)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
