package com.proactivediary.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.notes.components.EnvelopeCard
import com.proactivediary.ui.theme.DiaryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInboxScreen(
    onBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    onComposeNote: () -> Unit,
    viewModel: NoteInboxViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Notes", color = DiaryColors.Ink) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DiaryColors.Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DiaryColors.Paper
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onComposeNote,
                containerColor = DiaryColors.WineRed
            ) {
                Icon(Icons.Default.Add, "Send a note", tint = androidx.compose.ui.graphics.Color.White)
            }
        },
        containerColor = DiaryColors.Paper
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your inbox is waiting",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DiaryColors.Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Send a note first \u2014 kindness comes back.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = DiaryColors.Pencil,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    EnvelopeCard(
                        isRead = note.status == "read",
                        createdAt = note.createdAt,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }
}
