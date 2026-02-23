package com.proactivediary.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.PlusJakartaSans

@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacyControls: () -> Unit = {},
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val isLockEnabled by viewModel.isLockEnabled.collectAsState()
    val isHideContentEnabled by viewModel.isHideContentEnabled.collectAsState()
    val canUseBiometric by viewModel.canUseBiometric.collectAsState()

    var showDeleteCloudDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // ── Header ──────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
            Text(
                text = "Security",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── App Lock toggle ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "App Lock",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = if (canUseBiometric) "Biometric or PIN to open the app"
                    else "No biometric or PIN set up on this device",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Switch(
                checked = isLockEnabled,
                onCheckedChange = { viewModel.toggleLock(it) },
                enabled = canUseBiometric,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        SectionDivider()

        // ── Hide content in app switcher ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hide Content in App Switcher",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Show a blank screen in recent apps",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Switch(
                checked = isHideContentEnabled,
                onCheckedChange = { viewModel.toggleHideContent(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
        SectionDivider()

        // ── Privacy Controls ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPrivacyControls() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Privacy Controls",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Choose what data inspires your prompts",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        SectionDivider()

        Spacer(Modifier.height(24.dp))

        // ── Data section ────────────────────────────────────────
        Text(
            text = "Data",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDeleteCloudDialog = true }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Delete All Cloud Data",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.error
                )
            )
        }
        SectionDivider()

        Spacer(Modifier.height(24.dp))

        // ── Export section ───────────────────────────────────────
        Text(
            text = "Export",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.exportAllAsZip() }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Export All Data as ZIP",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Entries, images, and audio in one file",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Clearance for persistent bottom nav
        Spacer(Modifier.height(80.dp))
    }

    // ── Delete cloud data confirmation dialog ───────────────────
    if (showDeleteCloudDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCloudDialog = false },
            title = {
                Text(
                    text = "Delete Cloud Data",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all data stored in the cloud, including synced entries and backups. Your local data will not be affected.\n\nThis action cannot be undone.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllCloudData()
                    showDeleteCloudDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCloudDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
}
