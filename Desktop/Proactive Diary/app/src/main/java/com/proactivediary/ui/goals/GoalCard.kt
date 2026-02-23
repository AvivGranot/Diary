package com.proactivediary.ui.goals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.components.formatTime
import com.proactivediary.ui.components.parseStoredTime
import com.proactivediary.ui.theme.CardShape
import com.proactivediary.ui.theme.DiaryMotion
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PillShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goal: GoalUiState,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    val (hour, minute) = parseStoredTime(goal.reminderTime)

    // Ring animation
    val animatedProgress by animateFloatAsState(
        targetValue = (goal.progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "ring_progress"
    )

    // Press scale
    var isPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) DiaryMotion.CARD_PRESS_SCALE else 1f,
        animationSpec = DiaryMotion.BouncySpring,
        label = "card_scale"
    )

    // Compassionate copy based on weekly progress
    val compassionateCopy = when {
        goal.progressPercent >= 100 -> "Perfect week. You showed up every day."
        goal.progressPercent >= 70 -> "You showed up ${goal.streak} days \u2014 that\u2019s consistency."
        goal.progressPercent >= 40 -> "Every entry counts. Keep going."
        goal.progressPercent > 0 -> "You came back. That\u2019s what matters."
        else -> "A new week begins. No pressure."
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = { onLongPress() }
                )
            },
        shape = CardShape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(DiarySpacing.cardPadding)
        ) {
            // Top row: title + edit/delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${goal.frequency.label} \u00B7 ${formatTime(hour, minute)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = pencilColor
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Edit, "Edit", Modifier.size(18.dp), tint = pencilColor.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Delete, "Delete", Modifier.size(18.dp), tint = pencilColor.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(Modifier.height(DiarySpacing.md))

            // Center: circular progress ring + streak count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background track
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Track ring
                        drawArc(
                            color = pencilColor.copy(alpha = 0.15f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress ring
                        drawArc(
                            color = extendedColors.accent,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center: streak count
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${goal.streak}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "days",
                            style = MaterialTheme.typography.labelSmall,
                            color = pencilColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(DiarySpacing.sm))

            // Compassionate copy
            Text(
                text = compassionateCopy,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = pencilColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(DiarySpacing.md))

            // Check-in button — pill shaped
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (goal.checkedInToday) {
                    // Done state
                    Surface(
                        shape = PillShape,
                        color = extendedColors.accent,
                        modifier = Modifier.combinedClickable(onClick = {})
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Done for today",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Check-in state
                    var btnPressed by remember { mutableStateOf(false) }
                    val btnScale by animateFloatAsState(
                        targetValue = if (btnPressed) DiaryMotion.BUTTON_PRESS_SCALE else 1f,
                        animationSpec = DiaryMotion.BouncySpring,
                        label = "btn_scale"
                    )
                    Surface(
                        shape = PillShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.accent),
                        modifier = Modifier
                            .scale(btnScale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        btnPressed = true
                                        tryAwaitRelease()
                                        btnPressed = false
                                        onCheckIn()
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "I showed up today",
                            style = MaterialTheme.typography.labelLarge,
                            color = extendedColors.accent,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
