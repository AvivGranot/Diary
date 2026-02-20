package com.proactivediary.ui.goals

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
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.components.parseStoredTime
import com.proactivediary.ui.theme.CormorantGaramond
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun GoalsScreen(
    onBack: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsState()

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalUiState?>(null) }
    var goalToDelete by remember { mutableStateOf<GoalUiState?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                text = "Goals",
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
                    contentDescription = "Add goal",
                    tint = inkColor
                )
            }
        }

        if (goals.isEmpty()) {
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
                        text = "No goals yet",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            color = pencilColor
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add a goal",
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
                items(goals, key = { it.id }) { goal ->
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
        }
    }

    // Delete confirmation
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
                    viewModel.deleteGoal(goal.id)
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

    // Add dialog
    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, frequency, time, days ->
                viewModel.addGoal(title, frequency, time, days)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    if (editingGoal != null) {
        AddGoalDialog(
            editingGoal = editingGoal,
            onDismiss = { editingGoal = null },
            onSave = { title, frequency, time, days ->
                viewModel.updateGoal(editingGoal!!.id, title, frequency, time, days)
                editingGoal = null
            }
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

    val daysLabel = buildDaysLabel(goal.reminderDays)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Large time
            Text(
                text = timeDisplay,
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = inkColor
                )
            )
            // Goal title + frequency + days
            Text(
                text = "${goal.title} · ${goal.frequency.label}" +
                        if (daysLabel.isNotEmpty()) " · $daysLabel" else "",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = pencilColor
                ),
                maxLines = 1
            )
        }
    }
}

private fun buildDaysLabel(reminderDays: String): String {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return try {
        val days = com.google.gson.Gson()
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
