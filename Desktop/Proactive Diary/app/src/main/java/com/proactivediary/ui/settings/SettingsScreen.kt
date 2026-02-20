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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.auth.AuthViewModel
import com.proactivediary.ui.auth.AuthDialog
import com.proactivediary.ui.paywall.BillingViewModel
import com.proactivediary.ui.paywall.PaywallDialog
import com.proactivediary.ui.paywall.Plan
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.PlusJakartaSans

@Composable
fun SettingsScreen(
    onNavigateToLayout: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToTypewriter: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val isStreakEnabled by viewModel.isStreakEnabled.collectAsState()
    val isAIEnabled by viewModel.isAIEnabled.collectAsState()
    val entryCount by viewModel.entryCount.collectAsState()
    val activeReminderCount by viewModel.activeReminderCount.collectAsState()
    val activeGoalCount by viewModel.activeGoalCount.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val exportUri by viewModel.exportUri.collectAsState()
    val deleteStep by viewModel.deleteStep.collectAsState()
    val subscriptionState by billingViewModel.subscriptionState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPaywall by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showExportOptions by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfService by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity

    // Refresh notification health when returning from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNotificationHealth()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    LaunchedEffect(exportUri) {
        exportUri?.let { uri ->
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "*/*"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share export"))
            } catch (_: Exception) { }
            viewModel.clearExportUri()
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
            // Header with settings icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            // -- Layout --
            SettingsRow(
                label = "Layout",
                onClick = onNavigateToLayout
            )
            SettingsDivider()

            // -- My Goals --
            SettingsRow(
                label = "My Goals",
                value = "$activeGoalCount goals",
                onClick = onNavigateToGoals
            )
            SettingsDivider()

            // -- Reminders --
            SettingsRow(
                label = "Reminders",
                value = "$activeReminderCount active",
                onClick = onNavigateToReminders
            )
            SettingsDivider()

            // -- AI Insights toggle --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Insights",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Weekly writing pattern analysis",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Switch(
                    checked = isAIEnabled,
                    onCheckedChange = { viewModel.toggleAIInsights(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            if (isAIEnabled && entryCount < 7) {
                Text(
                    text = "You\u2019re ${7 - entryCount} entries away from your first insight.",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            SettingsDivider()

            // -- Subscription --
            SettingsRow(
                label = "Subscription",
                value = when (subscriptionState.plan) {
                    Plan.TRIAL -> "Free (${subscriptionState.trialDaysLeft} left)"
                    Plan.MONTHLY -> "Monthly"
                    Plan.ANNUAL -> "Annual"
                    Plan.EXPIRED -> "Expired"
                },
                onClick = { showPaywall = true }
            )
            SettingsDivider()

            // -- Export Data --
            Box {
                SettingsRow(
                    label = "Export Data",
                    value = "JSON / PDF",
                    onClick = { showExportOptions = true }
                )
                DropdownMenu(
                    expanded = showExportOptions,
                    onDismissRequest = { showExportOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export as JSON", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            viewModel.exportJson()
                            showExportOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export as PDF", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            viewModel.exportPdf()
                            showExportOptions = false
                        }
                    )
                }
            }
            SettingsDivider()

            // -- Privacy Policy --
            SettingsRow(
                label = "Privacy Policy",
                onClick = { showPrivacyPolicy = true }
            )
            SettingsDivider()

            // -- Terms of Service --
            SettingsRow(
                label = "Terms of Service",
                onClick = { showTermsOfService = true }
            )
            SettingsDivider()

            // -- Sign Out / Sign In --
            if (authViewModel.isAuthenticated) {
                // Show user info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = authState.userDisplayName ?: "Signed in",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (authState.userEmail != null) {
                            Text(
                                text = authState.userEmail!!,
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                    value = "Sync subscription",
                    onClick = { showAuthDialog = true }
                )
            }
            SettingsDivider()

            // -- Delete All Data --
            SettingsRow(
                label = "Delete All Data",
                onClick = { viewModel.startDeleteFlow() },
                isDestructive = true
            )

            Spacer(Modifier.height(32.dp))

            // Version number at bottom
            Text(
                text = "Version 1.0.0",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )
    }

    // Paywall dialog
    if (showPaywall) {
        val subState by billingViewModel.subscriptionState.collectAsState()
        PaywallDialog(
            onDismiss = { showPaywall = false },
            entryCount = subState.entryCount,
            totalWords = subState.totalWords,
            monthlyPrice = billingViewModel.getMonthlyPrice()?.let { "$it/month" } ?: "$5/month",
            annualPrice = billingViewModel.getAnnualPrice()?.let { "$it/year" } ?: "$40/year",
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

    // Auth dialog
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
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val sectionStyle = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onBackground)
                    val bodyStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Text("A private daily writing practice.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Data Storage", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("All your diary entries, goals, and settings are stored locally on your device. We never collect, transmit, or analyze what you write. Your words are yours alone.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Network Connections", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("Network connections: Google Play (subscriptions), Firebase (optional auth), Open-Meteo (weather data). If you enable AI Insights, your entry text is analyzed using the Google Gemini API. AI Insights are opt-in only.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Personal Information", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("If you choose to create an account, we store only your email address for subscription management. This is optional and not required to use the app.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Data Retention & Deletion", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("Your data persists until you delete it. Use Settings > Delete All Data to permanently erase everything. Uninstalling the app also removes all local data.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Third-Party Services", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("Google Play Billing: subscription management.\nFirebase Auth: optional account sign-in.\nFirebase Analytics: anonymous usage metrics (screens, features). Your diary content is never included.\nFirebase Crashlytics: anonymous crash reports to improve stability.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Children", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("This app is not directed at children under 13. We do not knowingly collect data from children.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Your Rights", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("You have full control over your data. You can export your writing at any time via Settings > Export Writing, and delete all data at any time.", style = bodyStyle)
                    Spacer(Modifier.height(12.dp))

                    Text("Contact", style = sectionStyle)
                    Spacer(Modifier.height(4.dp))
                    Text("Questions about this policy? Contact us at privacy@proactivediary.com", style = bodyStyle)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicy = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Terms of Service dialog
    if (showTermsOfService) {
        AlertDialog(
            onDismissRequest = { showTermsOfService = false },
            title = {
                Text(
                    text = "Terms of Service",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "By using Proactive Diary, you agree to our Terms of Service.\n\nYou retain full ownership of everything you write. Your content stays on your device and is never transmitted to our servers.\n\nSubscriptions are managed through Google Play and auto-renew unless cancelled. The app is provided \"as is\" without warranties. You are responsible for backing up your data.",
                        style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Read full terms online",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontStyle = FontStyle.Italic
                        ),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://proactivediary.com/terms-of-service"))
                            context.startActivity(intent)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsOfService = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
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
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all entries, goals, and settings. This cannot be undone.",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteStep1() }) {
                    Text("Continue", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                TextField(
                    value = deleteConfirmText,
                    onValueChange = { deleteConfirmText = it },
                    placeholder = { Text("DELETE", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = if (deleteConfirmText == "DELETE") MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.cancelDelete()
                    deleteConfirmText = ""
                }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 15.sp,
                color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null && value.isNotEmpty()) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.width(4.dp))
            }
            if (onClick != null && !isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
}
