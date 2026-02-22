package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.domain.model.DiaryThemeConfig

@Composable
fun WriteToolbar(
    wordCount: Int,
    showWordCount: Boolean,
    colorKey: String,
    richTextState: RichTextState? = null,
    onAttachmentClick: (() -> Unit)? = null,
    onPhotoClick: (() -> Unit)? = null,
    onDictateClick: (() -> Unit)? = null,
    onTemplatesClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    isDictating: Boolean = false,
    dictationSeconds: Int = 0,
    onStopDictation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = DiaryThemeConfig.colorForKey(colorKey)
    val textColor = DiaryThemeConfig.textColorFor(colorKey)
    val secondaryColor = DiaryThemeConfig.secondaryTextColorFor(colorKey)

    // Expanded state for Row 1 features
    var isExpanded by remember { mutableStateOf(false) }

    // + icon rotation
    val plusRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "plus_rotate"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(secondaryColor.copy(alpha = 0.15f))
        )

        // ── Row 1: Centered + / Expanded features ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(bgColor)
        ) {
            if (isDictating) {
                // Dictation mode fills Row 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DictationIndicator(
                        durationSeconds = dictationSeconds,
                        onStop = { onStopDictation?.invoke() },
                        secondaryColor = secondaryColor
                    )
                }
            } else if (isExpanded) {
                // Expanded: Photo | Dictate | Templates | Share | X
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InlineFeatureButton(
                                icon = Icons.Outlined.CameraAlt,
                                label = "Photo",
                                color = textColor,
                                onClick = {
                                    onPhotoClick?.invoke()
                                    isExpanded = false
                                }
                            )
                            InlineFeatureButton(
                                icon = Icons.Outlined.Mic,
                                label = "Dictate",
                                color = textColor,
                                onClick = {
                                    onDictateClick?.invoke()
                                    isExpanded = false
                                }
                            )
                            InlineFeatureButton(
                                icon = Icons.Outlined.Description,
                                label = "Templates",
                                color = textColor,
                                onClick = {
                                    onTemplatesClick?.invoke()
                                    isExpanded = false
                                }
                            )
                            InlineFeatureButton(
                                icon = Icons.Outlined.Person,
                                label = "Share",
                                color = textColor,
                                onClick = {
                                    onShareClick?.invoke()
                                    isExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Close (X) button
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(4.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isExpanded = false }
                    )
                }
            } else {
                // Collapsed: Centered +
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add",
                        tint = textColor,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(plusRotation)
                    )
                }
            }
        }

        // Divider between Row 1 and Row 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(secondaryColor.copy(alpha = 0.1f))
        )

        // ── Row 2: Formatting buttons ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(bgColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDictating) {
                    // During dictation, row 2 is empty
                    Spacer(Modifier.weight(1f))
                } else if (richTextState != null) {
                    // Inline styles — BLACK icons
                    FormatButton(
                        icon = Icons.Filled.FormatBold,
                        contentDescription = "Bold",
                        isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = {
                            richTextState.toggleSpanStyle(
                                androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    FormatButton(
                        icon = Icons.Filled.FormatItalic,
                        contentDescription = "Italic",
                        isActive = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = {
                            richTextState.toggleSpanStyle(
                                androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic)
                            )
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    FormatButton(
                        icon = Icons.Filled.FormatUnderlined,
                        contentDescription = "Underline",
                        isActive = richTextState.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.Underline,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = {
                            richTextState.toggleSpanStyle(
                                androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                            )
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    FormatButton(
                        icon = Icons.Filled.FormatStrikethrough,
                        contentDescription = "Strikethrough",
                        isActive = richTextState.currentSpanStyle.textDecoration == androidx.compose.ui.text.style.TextDecoration.LineThrough,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = {
                            richTextState.toggleSpanStyle(
                                androidx.compose.ui.text.SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                        }
                    )

                    // Separator
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(20.dp)
                            .background(secondaryColor.copy(alpha = 0.15f))
                    )
                    Spacer(Modifier.width(8.dp))

                    // List styles — BLACK icons
                    FormatButton(
                        icon = Icons.Filled.FormatListBulleted,
                        contentDescription = "Bullet list",
                        isActive = richTextState.isUnorderedList,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = { richTextState.toggleUnorderedList() }
                    )
                    Spacer(Modifier.width(4.dp))
                    FormatButton(
                        icon = Icons.Filled.FormatListNumbered,
                        contentDescription = "Numbered list",
                        isActive = richTextState.isOrderedList,
                        activeColor = textColor,
                        inactiveColor = textColor,
                        onClick = { richTextState.toggleOrderedList() }
                    )
                }

                Spacer(Modifier.weight(1f))

                // Word count — only visible at 10+ words, with milestone pulses
                if (showWordCount && wordCount >= 10) {
                    val milestones = listOf(100, 250, 500, 1000)
                    val isMilestone = wordCount in milestones
                    val countAlpha by animateFloatAsState(
                        targetValue = if (isMilestone) 0.6f else 0.4f,
                        animationSpec = tween(if (isMilestone) 400 else 200),
                        label = "wc_alpha"
                    )
                    val countScale by animateFloatAsState(
                        targetValue = if (isMilestone) 1.08f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "wc_scale"
                    )
                    Text(
                        text = "$wordCount words",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = secondaryColor.copy(alpha = countAlpha)
                        ),
                        modifier = Modifier.scale(countScale)
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineFeatureButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = color.copy(alpha = 0.7f)
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.5f)
            )
        )
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

    Box(
        modifier = Modifier
            .size(32.dp)
            .then(
                if (isActive) Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(activeColor.copy(alpha = 0.1f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
                .padding(4.dp)
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
}

/**
 * Compact dictation indicator for the toolbar.
 * Shows: breathing dot + waveform bars + "Finish" button.
 */
@Composable
private fun DictationIndicator(
    durationSeconds: Int,
    onStop: () -> Unit,
    secondaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dict")

    val dotAlpha by infiniteTransition.animateFloat(0.4f, 0.8f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "dot")

    val wave1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "w1")
    val wave2 by infiniteTransition.animateFloat(0.5f, 0.8f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "w2")
    val wave3 by infiniteTransition.animateFloat(0.2f, 1f, infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "w3")
    val wave4 by infiniteTransition.animateFloat(0.6f, 0.9f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "w4")
    val wave5 by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(280), RepeatMode.Reverse), label = "w5")

    val dictColor = Color(0xFF1565C0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = dictColor.copy(alpha = dotAlpha))
        }

        val waveHeights = listOf(wave1, wave2, wave3, wave4, wave5)
        Canvas(modifier = Modifier.width(24.dp).height(16.dp)) {
            val barWidth = size.width / 9f
            val gap = barWidth
            val maxH = size.height
            waveHeights.forEachIndexed { i, fraction ->
                val barH = maxH * fraction
                drawRoundRect(
                    color = dictColor,
                    topLeft = androidx.compose.ui.geometry.Offset(i * (barWidth + gap), (maxH - barH) / 2),
                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Finish",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = dictColor
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onStop)
                .padding(horizontal = 10.dp, vertical = 4.dp)
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
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                Text("Save", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
