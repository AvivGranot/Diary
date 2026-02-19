package com.proactivediary.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.ui.components.WheelTimePicker
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun ReminderManagementScreen(
    onBack: () -> Unit,
    viewModel: ReminderManagementViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<WritingReminderEntity?>(null) }
    var reminderToDelete by remember { mutableStateOf<WritingReminderEntity?>(null) }

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
    ) {
        // Header: back + title + add
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = inkColor
                )
            }
            Text(
                text = "Reminders",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 24.sp,
                    color = inkColor
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add reminder",
                    tint = inkColor
                )
            }
        }

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 80.dp)
                ) {
                    Text(
                        text = "No reminders yet",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            color = pencilColor
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a writing reminder",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = pencilColor.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                reminderToDelete = reminder
                                false
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                    Color(0xFFE57373).copy(alpha = 0.3f)
                                else Color.Transparent,
                                label = "swipe_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFD32F2F).copy(alpha = 0.7f)
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        ReminderRow(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onClick = { editingReminder = reminder }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = pencilColor.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }

    // Delete confirmation
    if (reminderToDelete != null) {
        val reminder = reminderToDelete!!
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Delete reminder?", style = MaterialTheme.typography.titleLarge, color = inkColor)
            },
            text = {
                Text(
                    "This reminder will be removed.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = pencilColor
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReminder(reminder)
                    reminderToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Cancel", color = pencilColor)
                }
            }
        )
    }

    if (showAddDialog) {
        ReminderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { time, days, fallback ->
                viewModel.addReminder(time, days, fallback)
                showAddDialog = false
            }
        )
    }

    editingReminder?.let { reminder ->
        ReminderDialog(
            existingReminder = reminder,
            onDismiss = { editingReminder = null },
            onSave = { time, days, fallback ->
                viewModel.updateReminder(reminder, time, days, fallback)
                editingReminder = null
            }
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: WritingReminderEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val activeColor = if (reminder.isActive) inkColor else pencilColor.copy(alpha = 0.4f)

    val hour = reminder.time.split(":").getOrNull(0)?.toIntOrNull() ?: 8
    val minute = reminder.time.split(":").getOrNull(1)?.toIntOrNull() ?: 0
    val timeDisplay = formatTime(hour, minute)

    val days = try {
        Gson().fromJson(reminder.days, Array<Int>::class.java).toList()
    } catch (_: Exception) {
        (0..6).toList()
    }
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daysText = when {
        days.size >= 7 -> "Every day"
        days.size == 5 && days.containsAll(listOf(0, 1, 2, 3, 4)) -> "Weekdays"
        days.size == 2 && days.containsAll(listOf(5, 6)) -> "Weekends"
        else -> days.mapNotNull { dayLabels.getOrNull(it) }.joinToString(", ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeDisplay,
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    color = activeColor
                )
            )
            Text(
                text = daysText,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = if (reminder.isActive) pencilColor else pencilColor.copy(alpha = 0.4f)
                )
            )
        }
        Switch(
            checked = reminder.isActive,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun ReminderDialog(
    existingReminder: WritingReminderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (time: String, days: String, fallbackEnabled: Boolean) -> Unit
) {
    val existingDays = existingReminder?.let {
        try {
            Gson().fromJson(it.days, Array<Int>::class.java).toList()
        } catch (_: Exception) { (0..6).toList() }
    } ?: (0..6).toList()

    val existingHour = existingReminder?.time?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
    val existingMinute = existingReminder?.time?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0

    var hour by remember { mutableIntStateOf(existingHour) }
    var minute by remember { mutableIntStateOf(existingMinute) }
    var selectedDays by remember { mutableStateOf(existingDays.toSet()) }
    var fallbackEnabled by remember { mutableStateOf(existingReminder?.fallbackEnabled ?: true) }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingReminder != null) "Edit Reminder" else "Add Reminder",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        text = {
            Column {
                WheelTimePicker(
                    hour = hour,
                    minute = minute,
                    onTimeChanged = { h, m -> hour = h; minute = m }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "DAYS",
                    style = TextStyle(
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val isSelected = index in selectedDays
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - index
                                    else selectedDays + index
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.take(2),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = fallbackEnabled,
                        onCheckedChange = { fallbackEnabled = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Nudge me if I haven't written",
                        style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val time = "%02d:%02d".format(hour, minute)
                    val daysJson = Gson().toJson(selectedDays.sorted().toList())
                    onSave(time, daysJson, fallbackEnabled)
                },
                enabled = selectedDays.isNotEmpty()
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.secondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
