package com.proactivediary.ui.settings

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.ui.theme.CormorantGaramond

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderManagementScreen(
    onBack: () -> Unit,
    viewModel: ReminderManagementViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<WritingReminderEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Writing Reminders",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No reminders yet",
                            style = TextStyle(
                                fontFamily = CormorantGaramond,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a writing reminder",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onEdit = { editingReminder = reminder },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
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
private fun ReminderCard(
    reminder: WritingReminderEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val days = try {
        Gson().fromJson(reminder.days, Array<Int>::class.java).toList()
    } catch (_: Exception) {
        (0..6).toList()
    }
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val activeDays = days.mapNotNull { dayLabels.getOrNull(it) }.joinToString(", ")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.time,
                    style = TextStyle(
                        fontSize = 22.sp,
                        color = if (reminder.isActive) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (days.size == 7) "Every day" else activeDays,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
                if (reminder.fallbackEnabled) {
                    Text(
                        text = "Nudge if not written",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val timePickerState = rememberTimePickerState(
        initialHour = existingHour,
        initialMinute = existingMinute,
        is24Hour = true
    )
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
                TimePicker(state = timePickerState)

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
                    val time = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
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
