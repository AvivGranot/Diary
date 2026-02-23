package com.proactivediary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.CardShape
import com.proactivediary.ui.theme.DiaryMotion
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PillShape
import kotlinx.coroutines.delay

// ── HeroSection ─────────────────────────────────────────────────────────
// Apple Store-style hero with large serif title + optional subtitle.
// Entrance: title fades + slides up, subtitle follows 200ms later.

@Composable
fun HeroSection(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    var titleVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        titleVisible = true
        delay(200)
        subtitleVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DiarySpacing.screenHorizontal,
                vertical = DiarySpacing.xxl
            )
    ) {
        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(DiarySpacing.xs))
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── GlassCard ───────────────────────────────────────────────────────────
// Semi-transparent card with subtle border. Press scales to 0.97.

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DiaryMotion.CARD_PRESS_SCALE else 1f,
        animationSpec = DiaryMotion.BouncySpring,
        label = "glass_card_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                                onClick()
                            }
                        )
                    }
                } else Modifier
            ),
        shape = shape,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(DiarySpacing.cardPadding),
            content = content
        )
    }
}

// ── FeatureTile ─────────────────────────────────────────────────────────
// Tappable tile with icon + title + description. Highlights when selected.

@Composable
fun FeatureTile(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) DiaryMotion.CARD_PRESS_SCALE else 1f,
        animationSpec = DiaryMotion.BouncySpring,
        label = "feature_tile_scale"
    )

    val backgroundColor = if (isSelected) {
        extendedColors.accent.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    val borderColor = if (isSelected) {
        extendedColors.accent.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        shape = RoundedCornerShape(DiarySpacing.sm),
        color = backgroundColor,
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(DiarySpacing.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DiarySpacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) extendedColors.accent
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── PrivacyBadge ────────────────────────────────────────────────────────
// Small pill badge with shield icon for trust signals.

@Composable
fun PrivacyBadge(
    text: String = "On-device only",
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Surface(
        modifier = modifier,
        shape = PillShape,
        color = extendedColors.success.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = extendedColors.success
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.success
            )
        }
    }
}
