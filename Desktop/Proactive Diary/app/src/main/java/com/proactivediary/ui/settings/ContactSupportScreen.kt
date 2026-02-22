package com.proactivediary.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.InstrumentSerif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(
    onBack: () -> Unit,
    viewModel: ContactSupportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.submitResult) {
        when (uiState.submitResult) {
            "sent" -> {
                snackbarHostState.showSnackbar("Request submitted successfully!")
                viewModel.clearResult()
            }
            "fallback" -> {
                val subject = if (uiState.category == "bug") {
                    "[BUG REPORT] ${uiState.subject}"
                } else {
                    "[CUSTOMER SUPPORT] ${uiState.subject}"
                }
                val body = buildString {
                    appendLine(uiState.description)
                    appendLine()
                    appendLine("--- Device Info ---")
                    appendLine("App Version: ${uiState.deviceInfo.appVersion}")
                    appendLine("Android: ${uiState.deviceInfo.androidVersion}")
                    appendLine("Device: ${uiState.deviceInfo.deviceModel}")
                    appendLine("Plan: ${uiState.deviceInfo.planType}")
                    appendLine("Entries: ${uiState.deviceInfo.totalEntries}")
                }
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@proactivediary.com"))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
                snackbarHostState.showSnackbar("Opening email client...")
                viewModel.clearResult()
            }
            "error" -> {
                snackbarHostState.showSnackbar("Failed to submit. Please try again.")
                viewModel.clearResult()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contact Support",
                        style = TextStyle(
                            fontFamily = InstrumentSerif,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Category selector
            Text(
                text = "CATEGORY",
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.category == "support",
                    onClick = { viewModel.setCategory("support") },
                    label = { Text("Customer Support", fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        selectedLabelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                FilterChip(
                    selected = uiState.category == "bug",
                    onClick = { viewModel.setCategory("bug") },
                    label = { Text("Bug Report", fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        selectedLabelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            // Email field
            Text(
                text = "EMAIL",
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.setEmail(it) },
                placeholder = { Text("your@email.com", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(16.dp))

            // Subject field
            Text(
                text = "SUBJECT",
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = { if (it.length <= 100) viewModel.setSubject(it) },
                placeholder = { Text("Brief summary", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
                singleLine = true,
                isError = uiState.subjectError != null,
                supportingText = uiState.subjectError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(16.dp))

            // Description field
            Text(
                text = "DESCRIPTION",
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.setDescription(it) },
                placeholder = { Text("Describe your issue or question...", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
                minLines = 4,
                maxLines = 8,
                isError = uiState.descriptionError != null,
                supportingText = uiState.descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    cursorColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(20.dp))

            // Device info card
            Text(
                text = "DEVICE INFO",
                style = TextStyle(
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DeviceInfoRow("App Version", uiState.deviceInfo.appVersion)
                    DeviceInfoRow("Android", uiState.deviceInfo.androidVersion)
                    DeviceInfoRow("Device", uiState.deviceInfo.deviceModel)
                    DeviceInfoRow("Plan", uiState.deviceInfo.planType)
                    DeviceInfoRow("Entries", uiState.deviceInfo.totalEntries.toString())
                }
            }

            Spacer(Modifier.height(24.dp))

            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (uiState.isSubmitting) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(enabled = !uiState.isSubmitting) { viewModel.submit() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Submit",
                        style = TextStyle(
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        )
        Text(
            text = value,
            style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        )
    }
}
