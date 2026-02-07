package com.proactivediary.ui.designstudio.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.designstudio.DesignStudioState
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun DesignSummaryCard(
    state: DesignStudioState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(
                color = DiaryColors.Parchment,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(20.dp)
    ) {
        // Header label
        Text(
            text = "YOUR DIARY",
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = DiaryColors.Pencil
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Summary line with color swatch
        AnimatedContent(
            targetState = state.summaryLabel,
            transitionSpec = {
                fadeIn(initialAlpha = 0f) togetherWith fadeOut(targetAlpha = 0f)
            },
            label = "summaryLabel"
        ) { label ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color swatch
                Spacer(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(DiaryThemeConfig.colorForKey(state.selectedColor))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = label,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = DiaryColors.Ink
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas style
        Text(
            text = "Canvas: ${state.canvasDisplayName}",
            fontSize = 13.sp,
            color = DiaryColors.Pencil
        )

        // Features count
        val enabledCount = state.features.count { it.value }
        Text(
            text = "$enabledCount features enabled",
            fontSize = 13.sp,
            color = DiaryColors.Pencil
        )

        // Personalization (if set)
        if (state.markText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${state.markText}\"",
                fontFamily = if (state.markFont == "serif") CormorantGaramond else null,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = DiaryColors.Ink
            )

            Text(
                text = "${state.markPosition.replaceFirstChar { it.uppercase() }} \u00B7 ${state.markFont.replaceFirstChar { it.uppercase() }}",
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = DiaryColors.Pencil
            )
        }
    }
}
