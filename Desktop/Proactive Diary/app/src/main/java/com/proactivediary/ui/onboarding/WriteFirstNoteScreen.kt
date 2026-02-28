package com.proactivediary.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.notes.ChannelPickerSheet
import com.proactivediary.ui.notes.ComposeNoteViewModel
import com.proactivediary.ui.notes.ShareIntentLauncher
import com.proactivediary.ui.write.resolveContact

@Composable
fun WriteFirstNoteScreen(
    onContinue: () -> Unit,
    analyticsService: AnalyticsService,
    viewModel: ComposeNoteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val screenStartTime = remember { System.currentTimeMillis() }
    var inviteSent by remember { mutableStateOf(false) }
    var showChannelPicker by remember { mutableStateOf(false) }
    // True once user picks Browse Contacts and selects a contact
    var browseContactsMode by remember { mutableStateOf(false) }

    // Track screen shown
    LaunchedEffect(Unit) {
        analyticsService.logOnboardingFirstNoteShown()
    }

    // Contact picker for Browse Contacts path
    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val contact = resolveContact(context, uri)
            viewModel.onContactSelected(
                name = contact?.displayName,
                phone = contact?.phone,
                email = contact?.email
            )
            browseContactsMode = true
        }
    }

    // Track note sent success
    LaunchedEffect(state.isSent, inviteSent) {
        if (state.isSent || inviteSent) {
            analyticsService.logOnboardingFirstNoteComplete(
                wordCount = state.charCount,
                durationMs = System.currentTimeMillis() - screenStartTime
            )
        }
    }

    // Celebration screen — warm background
    if (state.isSent || inviteSent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OnboardingScreenBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(OnboardingHorizontalPadding)
            ) {
                Text(
                    text = "You just made someone's day!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                OnboardingPrimaryButton(
                    text = "Continue",
                    onClick = onContinue
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(OnboardingHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Send an anonymous note\nto someone you care about",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Note compose area — white card with hairline border on linen bg
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    TextField(
                        value = state.content,
                        onValueChange = { viewModel.updateContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = {
                            Text(
                                "I'm grateful for you because...",
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${state.charCount}/${ComposeNoteViewModel.MAX_CHARS}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.moderationError != null) {
                Text(
                    text = state.moderationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Browse Contacts path: show recipient + send/invite buttons
            if (browseContactsMode && state.recipientName != null) {
                // Recipient card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnboardingInk.copy(alpha = 0.08f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("To: ${state.recipientName}", color = MaterialTheme.colorScheme.onBackground)
                        if (state.isResolving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 4.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // In-app send (recipient on app)
                if (state.recipientId != null) {
                    OnboardingPrimaryButton(
                        text = "Seal & Send",
                        onClick = { viewModel.sendNote() },
                        enabled = state.content.isNotBlank() && !state.isSending,
                        isLoading = state.isSending
                    )
                } else if (state.isRecipientOnApp == false) {
                    // Recipient not on app — show invite via share
                    OnboardingSecondaryButton(
                        text = "Share Invite Link",
                        onClick = {
                            analyticsService.logNoteInviteSent()
                            val shareText = ShareIntentLauncher.buildShareText(state.content)
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(sendIntent, "Invite via")
                            )
                            inviteSent = true
                        }
                    )
                }
            } else {
                // Default: single Send button that opens channel picker
                OnboardingPrimaryButton(
                    text = "Send",
                    onClick = {
                        if (viewModel.moderateContent()) {
                            showChannelPicker = true
                        }
                    },
                    enabled = state.content.isNotBlank()
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Channel picker bottom sheet
    if (showChannelPicker) {
        ChannelPickerSheet(
            onChannelSelected = { channel ->
                showChannelPicker = false
                analyticsService.logShareViaChannel(channel.key, state.charCount)
                ShareIntentLauncher.launch(context, channel, state.content)
                inviteSent = true
            },
            onBrowseContacts = {
                showChannelPicker = false
                contactPicker.launch(null)
            },
            onDismiss = { showChannelPicker = false }
        )
    }
}
