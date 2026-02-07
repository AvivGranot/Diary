package com.proactivediary.ui.designstudio.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

private data class FormOption(
    val key: String,
    val title: String,
    val description: String
)

private val formOptions = listOf(
    FormOption("focused", "Focused", "Single entry with generous margins"),
    FormOption("spacious", "Spacious", "Multiple entries with breathing room"),
    FormOption("compact", "Compact", "Dense layout for prolific writers")
)

@Composable
fun FormSection(
    selectedForm: String,
    onFormSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "02",
            title = "Form",
            subtitle = "How your thoughts take shape on the page"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            formOptions.forEach { option ->
                val isSelected = option.key == selectedForm

                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(durationMillis = 200),
                    label = "formAlpha_${option.key}"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .alpha(alpha)
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    DiaryColors.Parchment,
                                    RoundedCornerShape(4.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onFormSelected(option.key) }
                        )
                        .padding(start = if (isSelected) 0.dp else 3.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left accent border for selected
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(72.dp)
                                    .background(
                                        DiaryColors.Ink,
                                        RoundedCornerShape(
                                            topStart = 4.dp,
                                            bottomStart = 4.dp
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.padding(
                                start = if (isSelected) 16.dp else 16.dp,
                                end = 16.dp
                            )
                        ) {
                            Text(
                                text = option.title,
                                fontFamily = CormorantGaramond,
                                fontSize = 18.sp,
                                color = DiaryColors.Ink
                            )

                            Text(
                                text = option.description,
                                fontSize = 12.sp,
                                color = DiaryColors.Pencil
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(28.dp))
    }
}
