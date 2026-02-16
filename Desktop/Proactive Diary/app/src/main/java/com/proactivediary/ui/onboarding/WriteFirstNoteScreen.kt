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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.proactivediary.ui.notes.ComposeNoteViewModel
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.write.resolveContact

@Composable
fun WriteFirstNoteScreen(
    onContinue: () -> Unit,
    viewModel: ComposeNoteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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

    // If note was sent, show celebration and auto-continue
    if (state.isSent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DiaryColors.Paper),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Text(
                    text = "You just made\nsomeone's day!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DiaryColors.Ink,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiaryColors.ElectricIndigo
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
            .background(DiaryColors.Paper)
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
                text = "Write something kind\nto someone you care about",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = DiaryColors.Ink,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Envelope note area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAF6EE))
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
                                color = DiaryColors.Pencil.copy(alpha = 0.5f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = DiaryColors.Ink,
                            cursorColor = DiaryColors.ElectricIndigo
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${state.wordCount}/100 words",
                            style = MaterialTheme.typography.bodySmall,
                            color = DiaryColors.Pencil
                        )
                    }
                }
            }

            if (state.moderationError != null) {
                Text(
                    text = state.moderationError!!,
                    color = DiaryColors.CoralRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipient
            if (state.recipientName == null) {
                OutlinedButton(
                    onClick = { contactPicker.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                    Text("  Choose a Friend", color = DiaryColors.Ink)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DiaryColors.ElectricIndigo.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("To: ${state.recipientName}", color = DiaryColors.Ink)
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

            // Send button
            if (state.recipientId != null) {
                Button(
                    onClick = { viewModel.sendNote() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.content.isNotBlank() && !state.isSending,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiaryColors.ElectricIndigo
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
                Button(
                    onClick = {
                        val shareText = "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Invite via"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiaryColors.SunsetOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                    Text("  Invite & Send", fontSize = 16.sp)
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = DiaryColors.CoralRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip option
            TextButton(onClick = onContinue) {
                Text(
                    "Skip for now",
                    color = DiaryColors.Pencil,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
