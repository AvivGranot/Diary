package com.proactivediary.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Combined time + days picker composable.
 * @param timeHour hour in 24h format
 * @param timeMinute minute
 * @param selectedDays set of selected day indices (0=Mon..6=Sun)
 * @param onTimeChanged called with (hour, minute) when time is picked
 * @param onDayToggled called when a day chip is toggled
 * @param label optional label displayed above the picker
 */
@Composable
fun ReminderPicker(
    timeHour: Int,
    timeMinute: Int,
    selectedDays: Set<Int>,
    onTimeChanged: (Int, Int) -> Unit,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val context = LocalContext.current
    val formattedTime = formatTime(timeHour, timeMinute)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Time picker row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    TimePickerDialog(
                        context,
                        { _, hour, minute -> onTimeChanged(hour, minute) },
                        timeHour,
                        timeMinute,
                        false
                    ).show()
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = "Select time",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day selector
        DaySelector(
            selectedDays = selectedDays,
            onDayToggled = onDayToggled
        )
    }
}

/**
 * Formats hour and minute into a 12-hour display string.
 */
fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm)
}

/**
 * Formats hour and minute into HH:mm string for storage.
 */
fun formatTimeForStorage(hour: Int, minute: Int): String {
    return String.format(Locale.US, "%02d:%02d", hour, minute)
}

/**
 * Parses an "HH:mm" time string into a Pair of (hour, minute).
 */
fun parseStoredTime(time: String): Pair<Int, Int> {
    return try {
        val parts = time.split(":")
        Pair(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) {
        Pair(8, 0) // default 8:00 AM
    }
}
