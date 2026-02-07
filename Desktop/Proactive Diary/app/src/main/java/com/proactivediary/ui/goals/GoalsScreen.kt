package com.proactivediary.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsState()

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalUiState?>(null) }
    var longPressedGoal by remember { mutableStateOf<GoalUiState?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<GoalUiState?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Your Goals",
            style = MaterialTheme.typography.headlineLarge,
            color = inkColor
        )

        Spacer(Modifier.height(16.dp))

        if (goals.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No goals yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = inkColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Set a goal to start tracking your progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = pencilColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onCheckIn = { viewModel.checkIn(goal.id) },
                        onLongPress = { longPressedGoal = goal }
                    )
                }
            }
        }

        // Add new goal link
        Text(
            text = "+ Add new goal",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            ),
            color = inkColor,
            modifier = Modifier
                .clickable { showAddDialog = true }
                .padding(vertical = 12.dp)
        )
    }

    // Long-press action sheet
    if (longPressedGoal != null) {
        val goal = longPressedGoal!!
        AlertDialog(
            onDismissRequest = { longPressedGoal = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(goal.title, style = MaterialTheme.typography.titleLarge, color = inkColor)
            },
            text = null,
            confirmButton = {
                TextButton(onClick = {
                    editingGoal = goal
                    longPressedGoal = null
                }) {
                    Text("Edit", color = inkColor)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = goal
                    longPressedGoal = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm != null) {
        val goal = showDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete goal?", style = MaterialTheme.typography.titleLarge, color = inkColor) },
            text = {
                Text(
                    "Delete this goal and all check-ins?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = pencilColor
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal.id)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
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
