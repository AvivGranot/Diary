package com.proactivediary.ui.designstudio.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun ColorChip(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "colorChipScale"
    )

    val borderWidth = if (isSelected) 2.dp else 0.dp
    val borderColor = if (isSelected) DiaryColors.Ink else Color.Transparent

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(4.dp, CircleShape)
                } else {
                    Modifier.shadow(1.dp, CircleShape)
                }
            )
            .clip(CircleShape)
            .background(color, CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}
