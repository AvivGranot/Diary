package com.proactivediary.ui.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.InstrumentSerif
import java.time.LocalDate
import java.time.ZoneId

data class CapsulePreset(
    val label: String,
    val days: Long
)

private val presets = listOf(
    CapsulePreset("1 week", 7),
    CapsulePreset("1 month", 30),
    CapsulePreset("3 months", 90),
    CapsulePreset("6 months", 180),
    CapsulePreset("1 year", 365)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeCapsuleDatePicker(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seal as time capsule",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose when this letter opens. Until then, it stays sealed.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        FilledTonalButton(
                            onClick = {
                                val openDate = LocalDate.now().plusDays(preset.days)
                                val openMs = openDate.atStartOfDay(ZoneId.systemDefault())
                                    .toInstant().toEpochMilli()
                                onDateSelected(openMs)
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(preset.label)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}
