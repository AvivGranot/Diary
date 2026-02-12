package com.proactivediary.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
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
            // Formatting buttons (only when rich text state is available)
            if (richTextState != null) {
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
                FormatButton(
                    icon = Icons.Outlined.AutoAwesome,
                    contentDescription = "Writing suggestions",
                    isActive = false,
                    activeColor = textColor,
                    inactiveColor = secondaryColor.copy(alpha = 0.5f),
                    onClick = onSuggestionsClick
                )
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
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (isActive) activeColor else inactiveColor,
        modifier = Modifier
            .size(36.dp)
            .padding(6.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
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
