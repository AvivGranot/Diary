package com.proactivediary.ui.designstudio.sections

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.DiaryColors

private data class DetailOption(
    val key: String,
    val label: String,
    val description: String
)

private val detailOptions = listOf(
    DetailOption("auto_save", "Auto-save indicator", "Show a subtle dot when saving"),
    DetailOption("word_count", "Word count", "Display words written at the bottom"),
    DetailOption("daily_quote", "Daily quote", "Show an inspiring quote when you open your diary"),
    DetailOption("date_header", "Date header", "Display today\u2019s date above each entry")
)

@Composable
fun DetailsSection(
    features: Map<String, Boolean>,
    onToggleFeature: (String) -> Unit,
    selectedFontSize: String = "medium",
    onFontSizeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "05",
            title = "Details",
            subtitle = "The small things are never small"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Font Size selector
            Text(
                text = "Font Size",
                fontSize = 14.sp,
                color = DiaryColors.Ink
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("small" to "Small", "medium" to "Medium", "large" to "Large").forEach { (key, label) ->
                    val isSelected = selectedFontSize == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isSelected) Modifier.background(DiaryColors.Ink.copy(alpha = 0.04f))
                                    .border(1.dp, DiaryColors.Ink, RoundedCornerShape(8.dp))
                                else Modifier.background(DiaryColors.Pencil.copy(alpha = 0.06f))
                                    .border(1.dp, DiaryColors.Pencil.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onFontSizeSelected(key) }
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            color = if (isSelected) DiaryColors.Ink else DiaryColors.Pencil
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feature toggles
            detailOptions.forEach { option ->
                val isEnabled = features[option.key] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleFeature(option.key) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.label,
                            fontSize = 14.sp,
                            color = DiaryColors.Ink
                        )
                        Text(
                            text = option.description,
                            fontSize = 12.sp,
                            color = DiaryColors.Pencil
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    DiaryToggle(
                        checked = isEnabled,
                        onCheckedChange = { onToggleFeature(option.key) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun DiaryToggle(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track is 44dp wide, thumb is 20dp, padding 2dp each side → travel = 44 - 4 - 20 = 20dp
    val thumbOffsetDp by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "toggleThumb"
    )

    val trackColor = if (checked) Color(0xFF007AFF) else DiaryColors.Pencil.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCheckedChange
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(x = thumbOffsetDp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
