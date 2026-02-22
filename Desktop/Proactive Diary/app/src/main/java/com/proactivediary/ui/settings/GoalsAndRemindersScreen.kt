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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.proactivediary.data.db.entities.WritingReminderEntity
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.components.parseStoredTime
import com.proactivediary.ui.goals.AddGoalDialog
import com.proactivediary.ui.goals.GoalUiState
import com.proactivediary.ui.goals.GoalsViewModel
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans

/**
 * Combined Goals & Reminders screen.
 * Section 1: Goals (add/edit/delete/check-in)
 * Section 2: Reminders (add/edit/delete/toggle)
 */
@Composable
fun GoalsAndRemindersScreen(
    onBack: () -> Unit = {},
    goalsViewModel: GoalsViewModel = hiltViewModel(),
    remindersViewModel: ReminderManagementViewModel = hiltViewModel()
) {
    val goals by goalsViewModel.goals.collectAsState()
    val reminders by remindersViewModel.reminders.collectAsState()

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalUiState?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalUiState?>(null) }

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<WritingReminderEntity?>(null) }
    var reminderToDelete by remember { mutableStateOf<WritingReminderEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                text = "Goals & Reminders",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = inkColor
                ),
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── GOALS SECTION ──
            item {
                SectionHeader(
                    title = "GOALS",
                    onAdd = { showAddGoalDialog = true }
                )
            }

            if (goals.isEmpty()) {
                item {
                    EmptyHint(
                        text = "No goals yet",
                        hint = "Tap + to add a goal"
                    )
                }
            } else {
                items(goals, key = { "goal_${it.id}" }) { goal ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                goalToDelete = goal
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
                        GoalRow(
                            goal = goal,
                            onClick = { editingGoal = goal }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = pencilColor.copy(alpha = 0.12f)
                    )
                }
            }

            // ── REMINDERS SECTION ──
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(
                    title = "REMINDERS",
                    onAdd = { showAddReminderDialog = true }
                )
            }

            if (reminders.isEmpty()) {
                item {
                    EmptyHint(
                        text = "No reminders yet",
                        hint = "Tap + to add a writing reminder"
                    )
                }
            } else {
                items(reminders, key = { "rem_${it.id}" }) { reminder ->
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
                            onToggle = { remindersViewModel.toggleReminder(reminder) },
                            onClick = { editingReminder = reminder }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = pencilColor.copy(alpha = 0.12f)
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    // ── Dialogs ──

    // Delete goal confirmation
    if (goalToDelete != null) {
        val goal = goalToDelete!!
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete goal?", style = MaterialTheme.typography.titleLarge, color = inkColor) },
            text = {
                Text(
                    "Delete \"${goal.title}\" and all check-ins?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = pencilColor
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    goalsViewModel.deleteGoal(goal.id)
                    goalToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel", color = pencilColor)
                }
            }
        )
    }

    // Delete reminder confirmation
    if (reminderToDelete != null) {
        val reminder = reminderToDelete!!
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete reminder?", style = MaterialTheme.typography.titleLarge, color = inkColor) },
            text = {
                Text(
                    "This reminder will be removed.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = pencilColor
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    remindersViewModel.deleteReminder(reminder)
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

    // Add goal dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, frequency, time, days ->
                goalsViewModel.addGoal(title, frequency, time, days)
                showAddGoalDialog = false
            }
        )
    }

    // Edit goal dialog
    if (editingGoal != null) {
        AddGoalDialog(
            editingGoal = editingGoal,
            onDismiss = { editingGoal = null },
            onSave = { title, frequency, time, days ->
                goalsViewModel.updateGoal(editingGoal!!.id, title, frequency, time, days)
                editingGoal = null
            }
        )
    }

    // Add reminder dialog
    if (showAddReminderDialog) {
        ReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onSave = { time, days, fallback ->
                remindersViewModel.addReminder(time, days, fallback)
                showAddReminderDialog = false
            }
        )
    }

    // Edit reminder dialog
    editingReminder?.let { reminder ->
        ReminderDialog(
            existingReminder = reminder,
            onDismiss = { editingReminder = null },
            onSave = { time, days, fallback ->
                remindersViewModel.updateReminder(reminder, time, days, fallback)
                editingReminder = null
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add $title",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = hint,
            style = TextStyle(
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun GoalRow(
    goal: GoalUiState,
    onClick: () -> Unit
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val (hour, minute) = parseStoredTime(goal.reminderTime)
    val timeDisplay = formatTime(hour, minute)

    val daysLabel = buildGoalDaysLabel(goal.reminderDays)

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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = inkColor
                )
            )
            Text(
                text = "${goal.title} \u00B7 ${goal.frequency.label}" +
                        if (daysLabel.isNotEmpty()) " \u00B7 $daysLabel" else "",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = pencilColor
                ),
                maxLines = 1
            )
        }
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

    val hour = reminder.time.split(":").getOrNull(0)?.toIntOrNull() ?: 20
    val minute = reminder.time.split(":").getOrNull(1)?.toIntOrNull() ?: 30
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
                    fontSize = 32.sp,
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

private fun buildGoalDaysLabel(reminderDays: String): String {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return try {
        val days = Gson()
            .fromJson(reminderDays, Array<Int>::class.java)
            .toList()
        when {
            days.size >= 7 -> "Every day"
            days.size == 5 && days.containsAll(listOf(0, 1, 2, 3, 4)) -> "Weekdays"
            days.size == 2 && days.containsAll(listOf(5, 6)) -> "Weekends"
            else -> days.mapNotNull { dayLabels.getOrNull(it) }.joinToString(", ")
        }
    } catch (_: Exception) {
        "Every day"
    }
}
