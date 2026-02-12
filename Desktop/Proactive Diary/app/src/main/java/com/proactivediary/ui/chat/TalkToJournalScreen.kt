package com.proactivediary.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond
import kotlinx.coroutines.delay

private val Cream = Color(0xFFF3EEE7)
private val Ink = Color(0xFF313131)
private val InkLight = Color(0xFF787878)
private val InkFaint = Color(0xFFAAAAAA)
private val ChipBg = Color(0xFFE8E0D4)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TalkToJournalScreen(
    onBack: () -> Unit,
    viewModel: TalkToJournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Talk to Your Journal",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            color = Ink
                        )
                    )
                    if (state.entryCount > 0) {
                        Text(
                            text = "${state.entryCount} entries in memory",
                            style = TextStyle(
                                fontFamily = CormorantGaramond,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = InkFaint
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
        )

        when {
            state.isApiKeyMissing -> {
                ChatEmptyState(
                    title = "AI not configured",
                    subtitle = "Add your Claude API key in Settings \u2192 AI Insights to start chatting",
                    modifier = Modifier.weight(1f)
                )
            }
            state.entryCount < 3 -> {
                ChatEmptyState(
                    title = "Keep writing",
                    subtitle = "Write at least 3 entries before chatting with your journal",
                    modifier = Modifier.weight(1f)
                )
            }
            state.messages.isEmpty() && state.suggestedQuestions.isNotEmpty() -> {
                // Welcome state with sparkle icon and staggered chip entrance
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = InkLight.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Ask your journal anything",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 22.sp,
                            color = Ink,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Your AI companion has read all ${state.entryCount} entries\nand knows your story",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = InkLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(Modifier.height(32.dp))

                    // Staggered suggestion chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.suggestedQuestions.forEachIndexed { index, question ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 100L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(300)
                                ) + fadeIn(tween(300))
                            ) {
                                SuggestionChip(
                                    text = question,
                                    onClick = { viewModel.sendMessage(question) }
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                ChatMessageList(
                    messages = state.messages,
                    isLoading = state.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Input bar (always visible if API key exists and enough entries)
        if (!state.isApiKeyMissing && state.entryCount >= 3) {
            ChatInputBar(
                onSend = { viewModel.sendMessage(it) },
                enabled = !state.isLoading,
                placeholder = "Ask about your journal\u2026"
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = CormorantGaramond,
            fontSize = 14.sp,
            color = Ink
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
