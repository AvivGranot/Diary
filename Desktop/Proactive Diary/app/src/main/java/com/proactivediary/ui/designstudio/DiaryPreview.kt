package com.proactivediary.ui.designstudio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun DiaryPreview(
    state: DesignStudioState,
    modifier: Modifier = Modifier
) {
    val diaryColor = DiaryThemeConfig.colorForKey(state.selectedColor)
    val textColor = DiaryThemeConfig.textColorFor(state.selectedColor)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(state.selectedColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(DiaryColors.Paper)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Diary book preview with cross-fade
        AnimatedContent(
            targetState = Triple(state.selectedColor, state.selectedTexture, state.selectedCanvas),
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith
                    fadeOut(animationSpec = tween(150))
            },
            label = "diaryPreview"
        ) { (colorKey, texture, canvas) ->
            val color = DiaryThemeConfig.colorForKey(colorKey)
            val txtColor = DiaryThemeConfig.textColorFor(colorKey)
            val secColor = DiaryThemeConfig.secondaryTextColorFor(colorKey)
            val pageColor = DiaryThemeConfig.textureColorForKey(texture)
            val pageTextColor = DiaryThemeConfig.textureSecondaryTextColor(texture)

            // Diary book shape (cover = soul color)
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(200.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(4.dp),
                        ambientColor = DiaryColors.Shadow,
                        spotColor = DiaryColors.Shadow
                    )
                    .background(color, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Inner page (texture/touch color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(pageColor, RoundedCornerShape(2.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Diary title
                        Text(
                            text = "My Diary",
                            fontFamily = CormorantGaramond,
                            fontSize = 16.sp,
                            color = DiaryThemeConfig.textureTextColor(texture),
                            textAlign = TextAlign.Center
                        )

                        // Mini line preview
                        Spacer(modifier = Modifier.height(10.dp))
                        DiaryLinePreview(
                            canvas = canvas,
                            lineColor = pageTextColor.copy(alpha = 0.2f)
                        )

                        // Mark text if present
                        if (state.markText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.markText,
                                fontFamily = if (state.markFont == "serif") CormorantGaramond else null,
                                fontSize = 8.sp,
                                fontStyle = FontStyle.Italic,
                                color = pageTextColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Config label
        AnimatedContent(
            targetState = state.configLabel,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith
                    fadeOut(animationSpec = tween(150))
            },
            label = "configLabel"
        ) { label ->
            Text(
                text = label,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                color = DiaryColors.Pencil,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DiaryLinePreview(
    canvas: String,
    lineColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (canvas) {
            "lined" -> {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(1.dp)
                            .background(lineColor)
                    )
                }
            }
            "dotted" -> {
                repeat(3) {
                    Text(
                        text = "\u00B7  \u00B7  \u00B7  \u00B7  \u00B7  \u00B7  \u00B7",
                        fontSize = 8.sp,
                        color = lineColor,
                        letterSpacing = 1.sp
                    )
                }
            }
            "grid" -> {
                repeat(3) {
                    Text(
                        text = "\u250C\u2500\u252C\u2500\u252C\u2500\u2510",
                        fontSize = 7.sp,
                        color = lineColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            "numbered" -> {
                (1..3).forEach { num ->
                    Text(
                        text = "$num \u2500\u2500\u2500\u2500\u2500",
                        fontSize = 7.sp,
                        color = lineColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            else -> {
                // Blank - just some space
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
