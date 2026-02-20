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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.auth.AuthViewModel
import com.proactivediary.notifications.NotificationHealth
import com.proactivediary.notifications.HealthIssue
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
    onNavigateToSupport: () -> Unit = {},
    onNavigateToDiaryWrapped: () -> Unit = {},
    onNavigateToThemeEvolution: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val isStreakEnabled by viewModel.isStreakEnabled.collectAsState()
    val isAIEnabled by viewModel.isAIEnabled.collectAsState()
    val entryCount by viewModel.entryCount.collectAsState()
    val designSummary by viewModel.diaryDesignSummary.collectAsState()
    val activeReminderCount by viewModel.activeReminderCount.collectAsState()
    val activeGoalCount by viewModel.activeGoalCount.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val exportUri by viewModel.exportUri.collectAsState()
    val deleteStep by viewModel.deleteStep.collectAsState()
    val notificationHealth by viewModel.notificationHealth.collectAsState()
    val isBatteryOptimized by viewModel.isBatteryOptimized.collectAsState()
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
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share export"))
            } catch (_: Exception) { }
            viewModel.clearExportUri()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F1EB))
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

            // YOUR JOURNAL section
            SectionHeader("YOUR JOURNAL")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                SettingsRow(
                    label = "Your Journal",
                    value = "Past entries",
                    onClick = onNavigateToJournal
                )
            }

            Spacer(Modifier.height(24.dp))

            // APPEARANCE section
            SectionHeader("APPEARANCE")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                // Diary Design
                SettingsRow(
                    label = "Personalization",
                    value = designSummary,
                    onClick = onOpenDesignStudio
                )
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
                    label = "Send Test Notification",
                    onClick = { viewModel.sendTestNotification() }
                )
            }

            Spacer(Modifier.height(24.dp))

            // AI INSIGHTS section
            SectionHeader("AI INSIGHTS")
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly AI Insights",
                            style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Analyze your writing patterns weekly",
                            style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Switch(
                        checked = isAIEnabled,
                        onCheckedChange = { viewModel.toggleAIInsights(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                            checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                }
                if (isAIEnabled && entryCount < 7) {
                    Text(
                        text = "You\u2019re ${7 - entryCount} entries away from your first insight. Keep writing!",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    )
                }
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
                        value = "JSON / PDF",
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
                            text = { Text("Export as PDF") },
                            onClick = {
                                viewModel.exportPdf()
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

            // SUPPORT section (secondary)
            SectionHeaderSecondary("SUPPORT")
            Spacer(Modifier.height(8.dp))
            Column {
                SettingsRow(
                    label = "Contact Us",
                    onClick = onNavigateToSupport
                )
            }

            Spacer(Modifier.height(24.dp))

            // YOUR STORY section (secondary)
            SectionHeaderSecondary("YOUR STORY")
            Spacer(Modifier.height(8.dp))
            Column {
                SettingsRow(
                    label = "Theme Evolution",
                    value = "Writing & growth patterns",
                    onClick = onNavigateToThemeEvolution
                )
            }

            Spacer(Modifier.height(24.dp))

            // ACCOUNT section (secondary)
            SectionHeaderSecondary("ACCOUNT")
            Spacer(Modifier.height(8.dp))
            Column {
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
                        label = "Create Account / Sign In",
                        value = "Sync subscription",
                        onClick = { showAuthDialog = true }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ABOUT section (secondary)
            SectionHeaderSecondary("ABOUT")
            Spacer(Modifier.height(8.dp))
            Column {
                SettingsRow(
                    label = "Privacy Policy",
                    onClick = { showPrivacyPolicy = true }
                )
                SettingsDivider()
                SettingsRow(
                    label = "Terms of Service",
                    onClick = { showTermsOfService = true }
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

        // Snackbar host — elevated above bottom nav bar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )
    }

    // Paywall dialog — no auth gate before purchase
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
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val sectionStyle = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onBackground)
                    val bodyStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

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
                    Text("Close", color = MaterialTheme.colorScheme.onBackground)
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
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "By using Proactive Diary, you agree to our Terms of Service.\n\nYou retain full ownership of everything you write. Your content stays on your device and is never transmitted to our servers.\n\nSubscriptions are managed through Google Play and auto-renew unless cancelled. The app is provided \"as is\" without warranties. You are responsible for backing up your data.",
                        style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Read full terms online",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground,
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
private fun SectionHeaderSecondary(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAF9F5)
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
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    )
}
