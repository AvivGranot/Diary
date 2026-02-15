package com.proactivediary.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.domain.model.GoalFrequency
import com.proactivediary.ui.components.DaySelector
import com.proactivediary.ui.components.FrequencySelector
import com.proactivediary.ui.components.WheelTimePicker
import com.proactivediary.ui.theme.CormorantGaramond
import kotlinx.coroutines.launch

data class GoalInput(
    val title: String = "",
    val frequency: GoalFrequency = GoalFrequency.DAILY,
    val hour: Int = 6,
    val minute: Int = 0,
    val selectedDays: Set<Int> = (0..6).toSet()
)

data class ReminderInput(
    val label: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
)

@Composable
fun OnboardingGoalsScreen(
    onDone: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingGoalsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val goals = remember {
        mutableStateListOf(GoalInput())
    }

    val reminders = remember {
        mutableStateListOf(
            ReminderInput("Morning reflection", 6, 0),
            ReminderInput("Evening journal", 20, 0)
        )
    }

    var fallbackEnabled by remember { mutableStateOf(true) }

    // Shared save-and-finish logic — called after permission is resolved
    fun saveAndFinish() {
        scope.launch {
            val activeGoals = goals.filter { it.title.isNotBlank() }
            val activeReminders = reminders.filter { it.enabled }

            viewModel.saveOnboardingData(
                goals = activeGoals,
                reminders = activeReminders,
                fallbackEnabled = fallbackEnabled
            )

            onDone()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Notifications disabled. You can enable them in Settings.")
            }
        }
        // Save and schedule reminders AFTER permission dialog is resolved
        // (whether granted or denied — alarms are still set, notifications
        // will just be suppressed if denied)
        saveAndFinish()
    }

    fun handleDone() {
        // Request notification permission FIRST (API 33+), save after result
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                // Permission dialog opens — saveAndFinish() called in callback
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        // Permission already granted or API < 33 — proceed immediately
        saveAndFinish()
    }

    val inkColor = MaterialTheme.colorScheme.onBackground
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val paperColor = MaterialTheme.colorScheme.background
    val parchmentColor = MaterialTheme.colorScheme.surface

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = paperColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // --- Section 01: Writing Reminders ---
            Text(
                text = "01",
                style = MaterialTheme.typography.labelSmall,
                color = pencilColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Writing Reminders",
                style = MaterialTheme.typography.headlineLarge,
                color = inkColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Build the habit. We\u2019ll gently nudge you.",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = pencilColor
            )

            Spacer(Modifier.height(20.dp))

            OnboardingRemindersSection(
                reminders = reminders,
                onReminderChanged = { index, reminder -> reminders[index] = reminder },
                onReminderAdded = {
                    if (reminders.size < 5) {
                        reminders.add(ReminderInput("Custom reminder", 12, 0))
                    }
                },
                onReminderRemoved = { index -> reminders.removeAt(index) },
                canAddMore = reminders.size < 5,
                inkColor = inkColor,
                pencilColor = pencilColor
            )

            // --- Divider ---
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = pencilColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // --- Section 02: Personal Goals ---
            Text(
                text = "02",
                style = MaterialTheme.typography.labelSmall,
                color = pencilColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Personal Goals",
                style = MaterialTheme.typography.headlineLarge,
                color = inkColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Set intentions that matter to you.",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = pencilColor
            )

            Spacer(Modifier.height(20.dp))

            goals.forEachIndexed { index, goal ->
                GoalInputCard(
                    goal = goal,
                    onGoalChanged = { goals[index] = it },
                    onRemove = if (goals.size > 1) {
                        { goals.removeAt(index) }
                    } else null,
                    parchmentColor = parchmentColor,
                    inkColor = inkColor,
                    pencilColor = pencilColor
                )
                if (index < goals.lastIndex) {
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (goals.size < 3) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "+ Add another goal",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    color = inkColor,
                    modifier = Modifier.clickable { goals.add(GoalInput()) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // --- Fallback toggle ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fallback reminder",
                        style = MaterialTheme.typography.bodyLarge,
                        color = inkColor
                    )
                    Text(
                        text = "If you haven't written, we'll nudge you 30 min later",
                        style = MaterialTheme.typography.bodySmall,
                        color = pencilColor
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = fallbackEnabled,
                    onCheckedChange = { fallbackEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = parchmentColor,
                        checkedTrackColor = inkColor,
                        uncheckedThumbColor = pencilColor,
                        uncheckedTrackColor = parchmentColor,
                        uncheckedBorderColor = pencilColor.copy(alpha = 0.3f)
                    )
                )
            }

            // --- Divider ---
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = pencilColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // --- Done button ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(inkColor)
                    .clickable { handleDone() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DONE \u2014 LET\u2019S WRITE",
                    style = MaterialTheme.typography.labelLarge,
                    color = parchmentColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // --- Skip ---
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.bodySmall,
                color = pencilColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            viewModel.skipOnboarding()
                            onSkip()
                        }
                    }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GoalInputCard(
    goal: GoalInput,
    onGoalChanged: (GoalInput) -> Unit,
    onRemove: (() -> Unit)?,
    parchmentColor: androidx.compose.ui.graphics.Color,
    inkColor: androidx.compose.ui.graphics.Color,
    pencilColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(parchmentColor)
            .padding(16.dp)
    ) {
        Column {
            // Title row with optional remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = goal.title,
                    onValueChange = { onGoalChanged(goal.copy(title = it)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = inkColor),
                    cursorBrush = SolidColor(inkColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (goal.title.isEmpty()) {
                                Text(
                                    text = "What's your goal?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = pencilColor.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove goal",
                            tint = pencilColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Underline
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = pencilColor.copy(alpha = 0.2f), thickness = 1.dp)

            Spacer(Modifier.height(16.dp))

            // Frequency
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.bodySmall,
                color = pencilColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FrequencySelector(
                selected = goal.frequency,
                onSelected = { onGoalChanged(goal.copy(frequency = it)) }
            )

            Spacer(Modifier.height(16.dp))

            // Time
            Text(
                text = "Remind at",
                style = MaterialTheme.typography.bodySmall,
                color = pencilColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            WheelTimePicker(
                hour = goal.hour,
                minute = goal.minute,
                onTimeChanged = { h, m -> onGoalChanged(goal.copy(hour = h, minute = m)) }
            )

            Spacer(Modifier.height(16.dp))

            // Days
            Text(
                text = "Days",
                style = MaterialTheme.typography.bodySmall,
                color = pencilColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DaySelector(
                selectedDays = goal.selectedDays,
                onDayToggled = { day ->
                    val newDays = goal.selectedDays.toMutableSet()
                    if (newDays.contains(day)) newDays.remove(day) else newDays.add(day)
                    onGoalChanged(goal.copy(selectedDays = newDays))
                }
            )
        }
    }
}
