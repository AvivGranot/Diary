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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

private data class TextureOption(
    val key: String,
    val name: String,
    val description: String,
    val previewColor: Color
)

private val textureOptions = listOf(
    TextureOption("paper", "Paper", "Classic warm white with subtle grain", Color(0xFFF3EEE7)),
    TextureOption("parchment", "Parchment", "Aged golden tone with character", Color(0xFFF0E6D0)),
    TextureOption("linen", "Linen", "Soft woven texture, gentle and quiet", Color(0xFFEDE8E0)),
    TextureOption("smooth", "Smooth", "Clean and modern, no texture", Color(0xFFFAF9F5)),
    TextureOption("dark", "Dark", "Deep tones for evening writing", Color(0xFF2A2928))
)

@Composable
fun TouchSection(
    selectedTexture: String,
    onTextureSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "02",
            title = "Touch",
            subtitle = "The surface beneath your words"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            textureOptions.forEach { option ->
                val isSelected = option.key == selectedTexture

                val borderAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "textureBorder_${option.key}"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = DiaryColors.Ink.copy(alpha = borderAlpha),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = DiaryColors.Divider,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        )
                        .background(
                            if (isSelected) DiaryColors.Parchment else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTextureSelected(option.key) }
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color preview square
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(option.previewColor)
                                .then(
                                    if (option.key == "dark") {
                                        Modifier
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = DiaryColors.Divider,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = option.name,
                                fontFamily = CormorantGaramond,
                                fontSize = 18.sp,
                                color = DiaryColors.Ink
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = option.description,
                                fontSize = 12.sp,
                                color = DiaryColors.Pencil,
                                lineHeight = 16.sp
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
