package com.proactivediary.ui.debug

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.Experiment
import com.proactivediary.analytics.ExperimentOverrideStore
import com.proactivediary.analytics.ExperimentService
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun DebugExperimentScreen(
    experimentService: ExperimentService,
    overrideStore: ExperimentOverrideStore,
    onBack: () -> Unit
) {
    // Force recomposition when overrides change
    var refreshKey by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Text(
            text = "Experiment Overrides",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Debug only. Override experiment variants on this device.",
            style = TextStyle(
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(Modifier.height(16.dp))

        // Clear all button
        TextButton(
            onClick = {
                overrideStore.clearAll()
                refreshKey++
            }
        ) {
            Text(
                text = "Clear All Overrides",
                style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            )
        }

        Spacer(Modifier.height(8.dp))

        // One card per experiment
        Experiment.entries.forEach { experiment ->
            // Read current state (refreshKey forces reread)
            val _ = refreshKey
            val activeVariant = experimentService.getVariant(experiment)
            val override = overrideStore.getOverride(experiment)

            ExperimentCard(
                experiment = experiment,
                activeVariant = activeVariant,
                hasOverride = override != null,
                onSelectVariant = { variant ->
                    if (variant == null) {
                        overrideStore.setOverride(experiment, null)
                    } else {
                        overrideStore.setOverride(experiment, variant)
                    }
                    refreshKey++
                }
            )

            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text(
                text = "Back to Settings",
                style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
private fun ExperimentCard(
    experiment: Experiment,
    activeVariant: String,
    hasOverride: Boolean,
    onSelectVariant: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Experiment name
            Text(
                text = experiment.name.replace("_", " "),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Remote Config key
            Text(
                text = experiment.key,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(Modifier.height(8.dp))

            // Active variant display + dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active: $activeVariant${if (hasOverride) " (override)" else ""}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = if (hasOverride) FontWeight.Bold else FontWeight.Normal,
                        color = if (hasOverride) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface
                    )
                )

                Box {
                    Text(
                        text = "Change",
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(8.dp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Option to clear override
                        DropdownMenuItem(
                            text = { Text("Default (Remote Config)") },
                            onClick = {
                                onSelectVariant(null)
                                expanded = false
                            }
                        )

                        // One option per variant
                        experiment.variants.forEach { variant ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = variant,
                                        fontWeight = if (variant == activeVariant) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectVariant(variant)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
