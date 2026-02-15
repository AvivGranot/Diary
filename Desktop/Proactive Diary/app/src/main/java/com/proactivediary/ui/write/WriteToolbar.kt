package com.proactivediary.ui.write

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun WriteToolbar(
    wordCount: Int,
    showWordCount: Boolean,
    colorKey: String,
    richTextState: RichTextState? = null,
    onSuggestionsClick: (() -> Unit)? = null,
    onAttachmentClick: (() -> Unit)? = null,
    isRecording: Boolean = false,
    recordingSeconds: Int = 0,
    onStopRecording: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = DiaryThemeConfig.colorForKey(colorKey)
    val textColor = DiaryThemeConfig.textColorFor(colorKey)
    val secondaryColor = DiaryThemeConfig.secondaryTextColorFor(colorKey)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bgColor)
    ) {
        // Top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(secondaryColor.copy(alpha = 0.15f))
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "+" attachment button
            if (onAttachmentClick != null && !isRecording) {
                FormatButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add attachment",
                    isActive = false,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.6f),
                    onClick = onAttachmentClick
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(20.dp)
                        .background(secondaryColor.copy(alpha = 0.15f))
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Recording mode: show recording indicator instead of format buttons
            if (isRecording) {
                RecordingIndicator(
                    durationSeconds = recordingSeconds,
                    onStop = { onStopRecording?.invoke() },
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )
            } else if (richTextState != null) {
                // Inline styles group
                FormatButton(
                    icon = Icons.Filled.FormatBold,
                    contentDescription = "Bold",
                    isActive = richTextState.currentSpanStyle.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = {
                        richTextState.toggleSpanStyle(
                            androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FormatButton(
                    icon = Icons.Filled.FormatItalic,
                    contentDescription = "Italic",
                    isActive = richTextState.currentSpanStyle.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = {
                        richTextState.toggleSpanStyle(
                            androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FormatButton(
                    icon = Icons.Filled.FormatUnderlined,
                    contentDescription = "Underline",
                    isActive = richTextState.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.Underline,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = {
                        richTextState.toggleSpanStyle(
                            androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                        )
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FormatButton(
                    icon = Icons.Filled.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    isActive = richTextState.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = {
                        richTextState.toggleSpanStyle(
                            androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        )
                    }
                )

                // Separator
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(20.dp)
                        .background(secondaryColor.copy(alpha = 0.15f))
                )
                Spacer(modifier = Modifier.width(8.dp))

                // List styles group
                FormatButton(
                    icon = Icons.Filled.FormatListBulleted,
                    contentDescription = "Bullet list",
                    isActive = richTextState.isUnorderedList,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = { richTextState.toggleUnorderedList() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FormatButton(
                    icon = Icons.Filled.FormatListNumbered,
                    contentDescription = "Numbered list",
                    isActive = richTextState.isOrderedList,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.4f),
                    onClick = { richTextState.toggleOrderedList() }
                )
            }

            // Suggestions button
            if (onSuggestionsClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(20.dp)
                        .background(secondaryColor.copy(alpha = 0.15f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSuggestionsClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = "Ideas",
                        tint = secondaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Ideas",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = secondaryColor.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Word count
            if (showWordCount) {
                Text(
                    text = "$wordCount words",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = secondaryColor
                    )
                )
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "press"
    )

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (isActive) activeColor else inactiveColor,
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .padding(6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            }
    )
}

/**
 * Compact recording indicator for the toolbar.
 * Shows: pulsing red dot + waveform bars + timer + stop button
 */
@Composable
private fun RecordingIndicator(
    durationSeconds: Int,
    onStop: () -> Unit,
    textColor: Color,
    secondaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec")

    // Pulsing red dot
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot"
    )

    // 5 waveform bars
    val wave1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "w1")
    val wave2 by infiniteTransition.animateFloat(0.5f, 0.8f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "w2")
    val wave3 by infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "w3")
    val wave4 by infiniteTransition.animateFloat(0.6f, 0.9f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "w4")
    val wave5 by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(280), RepeatMode.Reverse), label = "w5")

    val redColor = Color(0xFFD32F2F)
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Pulsing red dot
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = redColor.copy(alpha = dotAlpha), radius = size.minDimension / 2)
        }

        // Waveform bars
        val waveHeights = listOf(wave1, wave2, wave3, wave4, wave5)
        Canvas(modifier = Modifier.width(24.dp).height(16.dp)) {
            val barWidth = size.width / 9f
            val gap = barWidth
            val maxH = size.height
            waveHeights.forEachIndexed { i, fraction ->
                val barH = maxH * fraction
                drawRoundRect(
                    color = redColor,
                    topLeft = androidx.compose.ui.geometry.Offset(i * (barWidth + gap), (maxH - barH) / 2),
                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                )
            }
        }

        // Timer
        Text(
            text = String.format("%d:%02d", minutes, seconds),
            style = TextStyle(fontFamily = FontFamily.Default, fontSize = 13.sp, color = redColor)
        )

        // Stop button
        Icon(
            imageVector = Icons.Filled.Stop,
            contentDescription = "Stop recording",
            tint = redColor,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onStop)
        )
    }
}

@Composable
internal fun TagInputDialog(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tagText by remember { mutableStateOf(currentTags.joinToString(", ")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DiaryColors.Paper,
        titleContentColor = DiaryColors.Ink,
        textContentColor = DiaryColors.Pencil,
        title = {
            Text(
                text = "Tags",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Separate tags with commas",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = DiaryColors.Pencil
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    placeholder = {
                        Text(
                            text = "personal, gratitude, goals...",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.sp,
                                color = DiaryColors.Pencil.copy(alpha = 0.5f)
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = DiaryColors.Ink,
                        unfocusedIndicatorColor = DiaryColors.Pencil.copy(alpha = 0.3f),
                        cursorColor = DiaryColors.Ink,
                        focusedTextColor = DiaryColors.Ink,
                        unfocusedTextColor = DiaryColors.Ink
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp
                    ),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = tagText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                onConfirm(parsed)
            }) {
                Text("Save", color = DiaryColors.Ink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DiaryColors.Pencil)
            }
        }
    )
}
