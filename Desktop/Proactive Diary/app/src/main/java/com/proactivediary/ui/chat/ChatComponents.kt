package com.proactivediary.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.data.ai.ChatMessage
import com.proactivediary.ui.theme.CormorantGaramond

private val Cream = Color(0xFFF3EEE7)
private val CreamDark = Color(0xFFE8E0D4)
private val Ink = Color(0xFF313131)
private val InkLight = Color(0xFF787878)
private val InkFaint = Color(0xFFAAAAAA)
private val AiBubble = Color(0xFFFAF7F2)
private val UserBubble = Color(0xFF4A4A4A)

/**
 * Shared chat message list used by Go Deeper and Talk to Journal.
 */
@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages, key = { it.timestamp }) { message ->
            AnimatedVisibility(visible = true, enter = fadeIn()) {
                ChatBubble(message = message)
            }
        }

        if (isLoading) {
            item {
                TypingIndicator()
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isUser) UserBubble else AiBubble
    val textColor = if (isUser) Color.White else Ink
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                style = TextStyle(
                    fontFamily = if (isUser) CormorantGaramond else CormorantGaramond,
                    fontSize = if (isUser) 15.sp else 16.sp,
                    fontStyle = if (!isUser) FontStyle.Normal else FontStyle.Normal,
                    color = textColor,
                    lineHeight = 22.sp
                )
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
            .background(AiBubble)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(InkFaint)
            )
        }
    }
}

/**
 * Chat input bar with text field and send button.
 */
@Composable
fun ChatInputBar(
    onSend: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String = "Write your thoughts\u2026",
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Cream)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkFaint
                    )
                )
            },
            textStyle = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 15.sp,
                color = Ink
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CreamDark,
                unfocusedContainerColor = CreamDark,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Ink
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank() && enabled) {
                        onSend(text.trim())
                        text = ""
                    }
                }
            ),
            singleLine = false,
            maxLines = 4,
            enabled = enabled
        )

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank() && enabled) {
                    onSend(text.trim())
                    text = ""
                }
            },
            enabled = text.isNotBlank() && enabled
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank() && enabled) Ink else InkFaint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Error/empty state for chat screens.
 */
@Composable
fun ChatEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 20.sp,
                    color = Ink
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkLight
                )
            )
        }
    }
}
