package com.proactivediary.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

private val Cream = Color(0xFFF3EEE7)
private val Ink = Color(0xFF313131)
private val InkLight = Color(0xFF787878)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoDeeperSheet(
    entryId: String,
    onDismiss: () -> Unit,
    viewModel: GoDeeperViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Initialize ViewModel with entryId when shown from EntryDetail
    LaunchedEffect(entryId) {
        viewModel.initialize(entryId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cream,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .background(Cream)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Go Deeper",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 22.sp,
                        color = Ink
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Reflect on what you just wrote",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkLight
                    )
                )
            }

            // Messages
            if (state.isApiKeyMissing) {
                ChatEmptyState(
                    title = "AI not configured",
                    subtitle = "Add your Claude API key in Settings \u2192 AI Insights",
                    modifier = Modifier.weight(1f)
                )
            } else if (state.error != null && state.messages.isEmpty()) {
                ChatEmptyState(
                    title = "Couldn\u2019t connect",
                    subtitle = state.error ?: "",
                    modifier = Modifier.weight(1f)
                )
            } else {
                ChatMessageList(
                    messages = state.messages,
                    isLoading = state.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }

            // Input bar
            ChatInputBar(
                onSend = { viewModel.sendMessage(it) },
                enabled = !state.isLoading && !state.isApiKeyMissing,
                placeholder = "Share your thoughts\u2026"
            )
        }
    }
}
