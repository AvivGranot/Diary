package com.proactivediary.ui.designstudio.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun MarkSection(
    markText: String,
    markPosition: String,
    markFont: String,
    onTextChanged: (String) -> Unit,
    onPositionSelected: (String) -> Unit,
    onFontSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "06",
            title = "Mark",
            subtitle = "Leave your name where it matters"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Text input — underline only, no box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                BasicTextField(
                    value = markText,
                    onValueChange = { if (it.length <= 20) onTextChanged(it) },
                    textStyle = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 16.sp,
                        color = DiaryColors.Ink
                    ),
                    cursorBrush = SolidColor(DiaryColors.Ink),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box {
                            if (markText.isEmpty()) {
                                Text(
                                    text = "Your initials, a word, a reminder\u2026",
                                    fontFamily = CormorantGaramond,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = DiaryColors.Pencil.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Underline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.BottomStart)
                        .background(if (isFocused) DiaryColors.Ink else DiaryColors.Pencil)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Character counter
            Text(
                text = "${markText.length} / 20",
                fontSize = 11.sp,
                color = DiaryColors.Pencil,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Position + Font chip selectors
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionChip(
                    label = "Header",
                    isSelected = markPosition == "header",
                    onClick = { onPositionSelected("header") }
                )
                OptionChip(
                    label = "Footer",
                    isSelected = markPosition == "footer",
                    onClick = { onPositionSelected("footer") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                OptionChip(
                    label = "Serif",
                    isSelected = markFont == "serif",
                    onClick = { onFontSelected("serif") }
                )
                OptionChip(
                    label = "Sans",
                    isSelected = markFont == "sans",
                    onClick = { onFontSelected("sans") }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) DiaryColors.Ink else DiaryColors.Pencil.copy(alpha = 0.2f)
    val bgColor = if (isSelected) DiaryColors.Ink.copy(alpha = 0.04f) else DiaryColors.Parchment

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = DiaryColors.Ink
        )
    }
}
