package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.theme.DiaryMotion
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Siri Suggestions–style contextual prompt card.
 * Appears above the editor with a time-aware greeting, a journaling prompt,
 * and subtle attribution. Swipe horizontally or tap X to dismiss.
 */
@Composable
fun ContextualPromptCard(
    prompt: String,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onPromptTap: ((String) -> Unit)? = null
) {
    val extendedColors = LocalDiaryExtendedColors.current
    var visible by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }

    // Staggered entrance
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    // Time-aware greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6  -> "Late night thoughts."
            hour < 12 -> "Good morning."
            hour < 17 -> "Good afternoon."
            hour < 21 -> "Good evening."
            else      -> "Winding down."
        }
    }

    if (!dismissed) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = DiaryMotion.GentleSpring.dampingRatio,
                    stiffness = DiaryMotion.GentleSpring.stiffness
                )
            ) { -it / 3 },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 3 }
        ) {
            GlassCard(
                modifier = modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (kotlin.math.abs(totalDrag) > 150f) {
                                    dismissed = true
                                    onDismiss()
                                }
                            }
                        )
                    }
            ) {
                Column {
                    // Header row: sparkle icon + greeting + close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = extendedColors.accent
                            )
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = InstrumentSerif
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Dismiss",
                            tint = secondaryTextColor.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    dismissed = true
                                    onDismiss()
                                }
                        )
                    }

                    Spacer(Modifier.height(DiarySpacing.sm))

                    // Prompt text — tappable to use as guided prompt
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (onPromptTap != null) Modifier.clickable { onPromptTap(prompt) }
                                else Modifier
                            )
                    )

                    Spacer(Modifier.height(DiarySpacing.sm))

                    // Attribution
                    Text(
                        text = "Based on your recent entries",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = secondaryTextColor.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

/**
 * Floating suggestion chips — shown when the editor is empty.
 * Compact pill chips that stagger in horizontally.
 */
@Composable
fun SuggestionChips(
    suggestions: List<String>,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier,
    onChipClick: (String) -> Unit = {}
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DiarySpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
    ) {
        suggestions.take(3).forEachIndexed { index, suggestion ->
            var chipVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 100L)
                chipVisible = true
            }

            AnimatedVisibility(
                visible = chipVisible,
                enter = fadeIn(tween(300, delayMillis = index * 100)) +
                        slideInVertically(tween(300, delayMillis = index * 100)) { it / 2 }
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.clickable { onChipClick(suggestion) },
                    shape = com.proactivediary.ui.theme.ChipShape,
                    color = secondaryTextColor.copy(alpha = 0.06f)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
