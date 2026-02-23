package com.proactivediary.ui.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PillShape

data class AIInsightData(
    val summary: String = "",
    val themes: List<String> = emptyList(),
    @Deprecated("Mood feature removed") val moodTrend: String? = null,
    val promptSuggestions: List<String> = emptyList(),
    val isAvailable: Boolean = false,
    val isLocked: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIInsightCard(
    data: AIInsightData,
    onUpgrade: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!data.isAvailable && !data.isLocked) return

    val extendedColors = LocalDiaryExtendedColors.current
    var isExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DiarySpacing.md, vertical = DiarySpacing.xxs)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f))
                .then(if (data.isLocked) Modifier.alpha(0.3f) else Modifier)
                .clickable { if (!data.isLocked) isExpanded = !isExpanded }
        ) {
            // Header: icon + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = extendedColors.accent
                )
                Spacer(Modifier.width(DiarySpacing.xs))
                Text(
                    text = "Your journal noticed\u2026",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(DiarySpacing.sm))

            // Summary — collapsed: 2 lines, expanded: all
            Text(
                text = data.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // Expanded content
            if (isExpanded) {
                Spacer(Modifier.height(DiarySpacing.sm))

                // Theme bars (visual data)
                if (data.themes.isNotEmpty()) {
                    data.themes.take(5).forEachIndexed { index, theme ->
                        val alphaLevel = 1f - (index * 0.15f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight((1f - index * 0.15f).coerceAtLeast(0.3f))
                                    .height(4.dp),
                                shape = PillShape,
                                color = extendedColors.accent.copy(alpha = alphaLevel)
                            ) {}
                            Spacer(Modifier.width(DiarySpacing.xs))
                            Text(
                                text = theme,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Suggested prompts
                if (data.promptSuggestions.isNotEmpty()) {
                    Spacer(Modifier.height(DiarySpacing.sm))
                    Text(
                        text = "Try writing about:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(DiarySpacing.xxs))
                    data.promptSuggestions.forEach { prompt ->
                        Text(
                            text = "\u2022 $prompt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(DiarySpacing.xs))

            // Attribution + expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Based on your recent entries",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!data.isLocked) {
                    Text(
                        text = if (isExpanded) "Show less" else "Show more",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = extendedColors.accent,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }
            }
        }

        // Locked overlay
        if (data.isLocked) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = PillShape,
                    color = extendedColors.accent,
                    onClick = onUpgrade
                ) {
                    Text(
                        text = "Unlock weekly insights",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
