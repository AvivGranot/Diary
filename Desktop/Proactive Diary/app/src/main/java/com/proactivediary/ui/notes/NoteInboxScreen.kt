package com.proactivediary.ui.notes

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.data.social.Note
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.write.resolveContact

private const val TAB_INBOX = 0
private const val TAB_COMPOSE = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInboxScreen(
    onBack: (() -> Unit)? = null,
    composeRequested: Boolean = false,
    onComposeRequestHandled: () -> Unit = {},
    viewModel: NoteInboxViewModel = hiltViewModel(),
    composeViewModel: ComposeNoteViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val unreadCount by viewModel.unreadCount.collectAsState(initial = 0)
    var selectedTab by remember { mutableIntStateOf(TAB_INBOX) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    // Handle external compose request (e.g. from QuotesScreen "Send a note")
    LaunchedEffect(composeRequested) {
        if (composeRequested) {
            selectedTab = TAB_COMPOSE
            onComposeRequestHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header: "Notes" + badge
        NotesHeader(unreadCount = unreadCount)

        Spacer(modifier = Modifier.height(12.dp))

        // Tab pills: NOTES INBOX | COMPOSE
        TabPills(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab content
        when (selectedTab) {
            TAB_INBOX -> NotesInboxContent(
                notes = notes,
                onNoteTap = { note ->
                    if (note.status != "read") {
                        viewModel.markAsRead(note.id)
                    }
                    selectedNote = note
                },
                onSendCtaTap = { selectedTab = TAB_COMPOSE },
                modifier = Modifier.weight(1f)
            )
            TAB_COMPOSE -> ComposeTabContent(
                composeViewModel = composeViewModel,
                onSwitchToInbox = {
                    composeViewModel.resetState()
                    selectedTab = TAB_INBOX
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom padding for nav
        Spacer(modifier = Modifier.height(64.dp))
    }

    // Note reveal bottom sheet
    if (selectedNote != null) {
        NoteRevealSheet(
            note = selectedNote!!,
            onDismiss = { selectedNote = null },
            onSendOneBack = {
                selectedNote = null
                selectedTab = TAB_COMPOSE
            }
        )
    }
}

@Composable
private fun NotesHeader(unreadCount: Int) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notes",
            fontFamily = InstrumentSerif,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$unreadCount new",
                    color = accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TabPills(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf("NOTES INBOX", "COMPOSE")
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun NotesInboxContent(
    notes: List<Note>,
    onNoteTap: (Note) -> Unit,
    onSendCtaTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        // Empty state with CTA
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SendCtaCard(onClick = onSendCtaTap)
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Your inbox is waiting",
                fontFamily = InstrumentSerif,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Kindness starts with you.\nSend your first note.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            item(key = "cta") {
                SendCtaCard(onClick = onSendCtaTap)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(notes, key = { it.id }) { note ->
                NoteListItem(
                    note = note,
                    onClick = { onNoteTap(note) }
                )
            }
        }
    }
}

@Composable
private fun SendCtaCard(onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val accentDark = LocalDiaryExtendedColors.current.accentDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Accent gradient circle with envelope icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFFF6B7F), Color(0xFFFF3B5C))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Mail,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Send a kind note",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Anonymous \u00B7 Takes 10 seconds",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NoteListItem(note: Note, onClick: () -> Unit) {
    val isUnread = note.status != "read"
    val accent = MaterialTheme.colorScheme.primary
    val notificationRed = Color(0xFFFF3B5C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUnread) notificationRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isUnread) Icons.Filled.Mail else Icons.Filled.MailOutline,
                contentDescription = null,
                tint = if (isUnread) notificationRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "From: Anonymous",
                fontSize = 13.sp,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (isUnread) {
                Text(
                    text = "Tap to reveal \u2728",
                    fontSize = 11.sp,
                    color = accent
                )
            } else {
                Text(
                    text = note.content.take(50),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Time + dot
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatRelativeTime(note.createdAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                PulsingDot()
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val accent = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(accent.copy(alpha = alpha), CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteRevealSheet(
    note: Note,
    onDismiss: () -> Unit,
    onSendOneBack: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "From: Anonymous",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = note.content,
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatRelativeTime(note.createdAt),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            val accent = MaterialTheme.colorScheme.primary
            Button(
                onClick = onSendOneBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Send one back",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTabContent(
    composeViewModel: ComposeNoteViewModel,
    onSwitchToInbox: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by composeViewModel.state.collectAsState()
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary
    var showChannelPicker by remember { mutableStateOf(false) }
    var inAppContactMode by remember { mutableStateOf(false) }

    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val contact = resolveContact(context, uri)
            composeViewModel.onContactSelected(
                name = contact?.displayName,
                phone = contact?.phone,
                email = contact?.email
            )
            inAppContactMode = true
        }
    }

    // Auto-switch to inbox on successful send
    LaunchedEffect(state.isSent) {
        if (state.isSent) {
            kotlinx.coroutines.delay(1500)
            onSwitchToInbox()
        }
    }

    if (state.isSent) {
        // Success state
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Sealed & sent \u2728",
                    fontFamily = InstrumentSerif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "They\u2019ll never know it was you.",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Header: back arrow + title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onSwitchToInbox, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Send a Note",
                fontFamily = InstrumentSerif,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TextField(
                    value = state.content,
                    onValueChange = { composeViewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = {
                        Text(
                            "Write something kind...",
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = accent
                    )
                )

                // Counter + emoji row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.charCount}/${ComposeNoteViewModel.MAX_CHARS}",
                        fontSize = 10.sp,
                        color = if (state.charCount >= ComposeNoteViewModel.MAX_CHARS)
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        Icons.Outlined.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Moderation error
        if (state.moderationError != null) {
            Text(
                text = state.moderationError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // In-app contact path: recipient card + Seal & Send
        if (inAppContactMode && state.recipientName != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "To: ${state.recipientName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedVisibility(visible = !state.isResolving) {
                        when (state.isRecipientOnApp) {
                            true -> Text("Ready to send!", color = accent, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            false -> Text("Not on the app yet \u2014 invite them!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            null -> {}
                        }
                    }
                    if (state.isResolving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp).padding(top = 4.dp), strokeWidth = 2.dp, color = accent)
                    }
                }
            }
            Text(
                text = "Change recipient",
                color = accent,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable { contactPicker.launch(null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isRecipientOnApp == true) {
                SealAndSendButton(
                    enabled = state.content.isNotBlank() && !state.isSending,
                    isSending = state.isSending,
                    onClick = { composeViewModel.sendNote() }
                )
            } else if (state.isRecipientOnApp == false) {
                Button(
                    onClick = { showChannelPicker = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = state.content.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = accent.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invite via...", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Default: Share via... (primary) + Send in-app (secondary)
            Button(
                onClick = {
                    if (composeViewModel.moderateContent()) {
                        showChannelPicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = state.content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = accent.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share via...", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    if (composeViewModel.moderateContent()) {
                        contactPicker.launch(null)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = state.content.isNotBlank(),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (state.content.isNotBlank()) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send in-app", color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Error display
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer
        Text(
            text = "Anonymous \u00B7 Reviewed for kindness",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Channel picker bottom sheet
    if (showChannelPicker) {
        ChannelPickerSheet(
            onChannelSelected = { channel ->
                showChannelPicker = false
                ShareIntentLauncher.launch(context, channel, state.content)
            },
            onBrowseContacts = {
                showChannelPicker = false
                contactPicker.launch(null)
            },
            onDismiss = { showChannelPicker = false }
        )
    }
}

@Composable
private fun SealAndSendButton(
    enabled: Boolean,
    isSending: Boolean,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            disabledContainerColor = accent.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(50)
    ) {
        if (isSending) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Filled.Mail, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Seal & Send", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatRelativeTime(timestampMs: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestampMs
    val minutes = diffMs / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}w"
    }
}
