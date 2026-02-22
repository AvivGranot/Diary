package com.proactivediary.ui.onboarding

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.proactivediary.ui.notes.ComposeNoteViewModel
import com.proactivediary.ui.write.resolveContact

@Composable
fun WriteFirstNoteScreen(
    channel: String = "contacts",
    contactName: String? = null,
    contactPhone: String? = null,
    contactEmail: String? = null,
    onContinue: () -> Unit,
    analyticsService: AnalyticsService,
    viewModel: ComposeNoteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val screenStartTime = remember { System.currentTimeMillis() }
    var inviteSent by remember { mutableStateOf(false) }

    // Track screen shown
    LaunchedEffect(Unit) {
        analyticsService.logOnboardingFirstNoteShown()
    }

    // Auto-populate contact if passed from WelcomeScreen
    LaunchedEffect(contactName) {
        if (contactName != null) {
            viewModel.onContactSelected(
                name = contactName,
                phone = contactPhone,
                email = contactEmail
            )
        }
    }

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
        }
    }

    // Track note sent success
    LaunchedEffect(state.isSent, inviteSent) {
        if (state.isSent || inviteSent) {
            analyticsService.logOnboardingFirstNoteComplete(
                wordCount = state.wordCount,
                durationMs = System.currentTimeMillis() - screenStartTime
            )
        }
    }

    // If note was sent (either via in-app or invite), show celebration
    if (state.isSent || inviteSent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
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
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continue", fontSize = 16.sp)
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
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

            // Envelope note area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
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
                            text = "${state.wordCount}/100 words",
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

            // ── Channel-aware recipient + send section ──

            when (channel) {
                "whatsapp", "instagram" -> {
                    // WhatsApp/Instagram: no contact picker needed, just the note + send button
                    val platformName = if (channel == "whatsapp") "WhatsApp" else "Instagram"
                    val packageName = if (channel == "whatsapp") "com.whatsapp" else "com.instagram.android"
                    val iconTint = if (channel == "whatsapp") Color(0xFF25D366) else Color(0xFFE4405F)
                    val icon = if (channel == "whatsapp") Icons.Outlined.Chat else Icons.Outlined.CameraAlt

                    Button(
                        onClick = {
                            analyticsService.logNoteInviteSent()
                            val shareText = if (state.content.isNotBlank()) {
                                "Someone sent you an anonymous note via Proactive Diary:\n\n\"${state.content}\"\n\nDownload the app to send one back: https://play.google.com/store/apps/details?id=com.proactivediary"
                            } else {
                                "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                                setPackage(packageName)
                            }
                            try {
                                context.startActivity(sendIntent)
                            } catch (_: Exception) {
                                // App not installed — fallback to generic share
                                val fallback = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(fallback, "Share via"))
                            }
                            inviteSent = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = state.content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Send via $platformName",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                else -> {
                    // Contacts channel — existing behavior with fix

                    // Recipient
                    if (state.recipientName == null) {
                        OutlinedButton(
                            onClick = { contactPicker.launch(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                            Text("  Choose a Friend", color = MaterialTheme.colorScheme.onBackground)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("To: ${state.recipientName}", color = MaterialTheme.colorScheme.onBackground)
                                if (state.isResolving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp).padding(top = 4.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Send button — recipient on app
                    if (state.recipientId != null) {
                        Button(
                            onClick = { viewModel.sendNote() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = state.content.isNotBlank() && !state.isSending,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                                Text("  Seal & Send", fontSize = 16.sp)
                            }
                        }
                    } else if (state.isRecipientOnApp == false) {
                        // Recipient NOT on app — show platform share buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // WhatsApp invite
                            Button(
                                onClick = {
                                    analyticsService.logNoteInviteSent()
                                    val shareText = "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                        setPackage("com.whatsapp")
                                    }
                                    try {
                                        context.startActivity(sendIntent)
                                    } catch (_: Exception) {
                                        val fallback = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(fallback, "Invite via"))
                                    }
                                    inviteSent = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.Chat, null, modifier = Modifier.size(20.dp))
                                Text("  Invite via WhatsApp", fontSize = 16.sp)
                            }

                            // Instagram invite
                            Button(
                                onClick = {
                                    analyticsService.logNoteInviteSent()
                                    val shareText = "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                        setPackage("com.instagram.android")
                                    }
                                    try {
                                        context.startActivity(sendIntent)
                                    } catch (_: Exception) {
                                        val fallback = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(fallback, "Invite via"))
                                    }
                                    inviteSent = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE4405F)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.CameraAlt, null, modifier = Modifier.size(20.dp))
                                Text("  Invite via Instagram", fontSize = 16.sp)
                            }

                            // Generic share
                            OutlinedButton(
                                onClick = {
                                    analyticsService.logNoteInviteSent()
                                    val shareText = "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Invite via"))
                                    inviteSent = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                                Text("  Share Invite Link", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
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
}
