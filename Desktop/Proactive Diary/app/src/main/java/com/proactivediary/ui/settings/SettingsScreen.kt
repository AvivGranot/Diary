package com.proactivediary.ui.settings

import android.app.Activity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.auth.AuthViewModel
import com.proactivediary.ui.auth.AuthDialog
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.Plan
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun SettingsScreen(
    onOpenDesignStudio: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToTypewriter: () -> Unit = {},
    onNavigateToYearInReview: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isStreakEnabled by viewModel.isStreakEnabled.collectAsState()
    val designSummary by viewModel.diaryDesignSummary.collectAsState()
    val activeReminderCount by viewModel.activeReminderCount.collectAsState()
    val activeGoalCount by viewModel.activeGoalCount.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val deleteStep by viewModel.deleteStep.collectAsState()
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPaywall by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Screen title
            Text(
                text = "Settings",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(24.dp))

            // APPEARANCE section
            SectionHeader("APPEARANCE")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                // Diary Design
                SettingsRow(
                    label = "Diary Design",
                    value = designSummary,
                    onClick = onOpenDesignStudio
                )
                SettingsDivider()

                // Dark Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
                SettingsDivider()

                // Font Size
                Box {
                    SettingsRow(
                        label = "Font Size",
                        value = fontSize.replaceFirstChar { it.uppercase() },
                        onClick = { showFontSizeMenu = true }
                    )
                    DropdownMenu(
                        expanded = showFontSizeMenu,
                        onDismissRequest = { showFontSizeMenu = false }
                    ) {
                        listOf("small" to "Small (14sp)", "medium" to "Medium (16sp)", "large" to "Large (18sp)").forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.setFontSize(key)
                                    showFontSizeMenu = false
                                }
                            )
                        }
                    }
                }
                SettingsDivider()

                // Writing Streak toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Writing Practice",
                            style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Show practice day counter on Write tab",
                            style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Switch(
                        checked = isStreakEnabled,
                        onCheckedChange = { viewModel.toggleStreak(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // NOTIFICATIONS section
            SectionHeader("NOTIFICATIONS")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow(
                    label = "Writing Reminders",
                    value = "$activeReminderCount active",
                    onClick = onNavigateToReminders
                )
                SettingsDivider()
                SettingsRow(
                    label = "My Goals",
                    value = "$activeGoalCount goals",
                    onClick = onNavigateToGoals
                )
                SettingsDivider()
                SettingsRow(
                    label = "Goal Reminders",
                    value = "$activeGoalCount active",
                    onClick = onNavigateToGoals
                )
            }

            Spacer(Modifier.height(24.dp))

            // SUBSCRIPTION section
            SectionHeader("SUBSCRIPTION")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                // Plan status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Plan: ${
                            when (subscriptionState.plan) {
                                Plan.TRIAL -> "Free (${subscriptionState.trialDaysLeft} entries left)"
                                Plan.MONTHLY -> "Monthly plan"
                                Plan.ANNUAL -> "Annual plan"
                                Plan.LIFETIME -> "Lifetime plan"
                                Plan.EXPIRED -> "Trial expired"
                            }
                        }",
                        style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                if (subscriptionState.plan == Plan.TRIAL || subscriptionState.plan == Plan.EXPIRED) {
                    // Upgrade button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { showPaywall = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Upgrade to Pro",
                            style = TextStyle(
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    SettingsDivider()

                    // Restore purchases
                    TextButton(
                        onClick = { billingViewModel.restorePurchases() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Restore Purchases",
                            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // DATA section
            SectionHeader("DATA")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                Box {
                    SettingsRow(
                        label = "Export Writing",
                        value = "JSON / Text",
                        onClick = { showExportOptions = true }
                    )
                    DropdownMenu(
                        expanded = showExportOptions,
                        onDismissRequest = { showExportOptions = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export as JSON") },
                            onClick = {
                                viewModel.exportJson()
                                showExportOptions = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export as Plain Text") },
                            onClick = {
                                viewModel.exportText()
                                showExportOptions = false
                            }
                        )
                    }
                }
                SettingsDivider()
                SettingsRow(
                    label = "Delete All Data",
                    value = "",
                    onClick = { viewModel.startDeleteFlow() },
                    isDestructive = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // YOUR BOOK section
            SectionHeader("YOUR BOOK")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow(
                    label = "Year in Review",
                    value = "${java.time.LocalDate.now().year}",
                    onClick = onNavigateToYearInReview
                )
            }

            Spacer(Modifier.height(24.dp))

            // ACCOUNT section
            SectionHeader("ACCOUNT")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                if (authViewModel.isAuthenticated) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = authState.userDisplayName ?: "Signed in",
                                style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            )
                            if (authState.userEmail != null) {
                                Text(
                                    text = authState.userEmail!!,
                                    style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }
                    }
                    SettingsDivider()
                    SettingsRow(
                        label = "Sign Out",
                        onClick = { authViewModel.signOut() }
                    )
                } else {
                    SettingsRow(
                        label = "Sign In",
                        value = "Optional",
                        onClick = { showAuthDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ABOUT section
            SectionHeader("ABOUT")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow(
                    label = "Privacy Policy",
                    onClick = { showPrivacyPolicy = true }
                )
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Version 1.0.0",
                        style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Paywall dialog — no auth gate before purchase
    if (showPaywall) {
        val subState by billingViewModel.subscriptionState.collectAsState()
        PaywallDialog(
            onDismiss = { showPaywall = false },
            entryCount = subState.entryCount,
            totalWords = subState.totalWords,
            onSelectPlan = { sku ->
                activity?.let { billingViewModel.launchPurchase(it, sku) }
                showPaywall = false
            },
            onRestore = {
                billingViewModel.restorePurchases()
                showPaywall = false
            }
        )
    }

    // Auth dialog — for optional account sign-in
    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onAuthenticated = { showAuthDialog = false }
        )
    }

    // Privacy Policy dialog
    if (showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicy = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "A private daily writing practice.\n\nAll your data stays on this device. We never collect, transmit, or analyze what you write. Your words are yours alone.\n\nThe only network connection this app makes is to Google Play for subscription management. Nothing you write ever leaves your device.",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Delete confirmation dialogs
    if (deleteStep == 1) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = {
                Text(
                    text = "Delete All Data",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all entries, goals, and settings. This cannot be undone.",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteStep1() }) {
                    Text("Continue", color = Color(0xFF8B3A3A))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (deleteStep == 2) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = {
                Text(
                    text = "Type DELETE to confirm",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                TextField(
                    value = deleteConfirmText,
                    onValueChange = { deleteConfirmText = it },
                    placeholder = { Text("DELETE") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.executeDeleteAll {
                            deleteConfirmText = ""
                            onNavigateToTypewriter()
                        }
                    },
                    enabled = deleteConfirmText == "DELETE"
                ) {
                    Text(
                        "Delete",
                        color = if (deleteConfirmText == "DELETE") Color(0xFF8B3A3A) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.cancelDelete()
                    deleteConfirmText = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Export options dialog
    if (showExportOptions) {
        // Handled by DropdownMenu above
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                color = if (isDestructive) Color(0xFF8B3A3A) else MaterialTheme.colorScheme.onSurface
            )
        )
        if (value != null && value.isNotEmpty()) {
            Text(
                text = value,
                style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    )
}
