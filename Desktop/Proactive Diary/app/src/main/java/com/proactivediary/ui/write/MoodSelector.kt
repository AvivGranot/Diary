package com.proactivediary.ui.write

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.proactivediary.domain.model.Mood

private val InkColor = Color(0xFF313131)

@Composable
fun MoodSelector(
    selectedMood: Mood?,
    onMoodSelected: (Mood?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Mood.entries.forEach { mood ->
            val isSelected = selectedMood == mood

            val circleSize by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 28.dp,
                animationSpec = tween(durationMillis = 200),
                label = "moodSize"
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .semantics { contentDescription = mood.label }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onMoodSelected(mood)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .background(
                            color = if (isSelected) mood.color else mood.color.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = InkColor,
                                    shape = CircleShape
                                )
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}
