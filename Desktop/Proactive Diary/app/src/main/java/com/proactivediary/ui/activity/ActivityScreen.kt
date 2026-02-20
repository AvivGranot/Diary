package com.proactivediary.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.PlusJakartaSans

/**
 * Activity tab -- notification feed showing likes, notes, comments, and rank changes.
 * V3 wireframe: serif header, grouped sections (NEW / THIS WEEK / EARLIER),
 * circular icon avatars per type, bold title + muted subtitle + relative timestamp.
 */
@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Text(
            text = "Activity",
            fontFamily = CormorantGaramond,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = DiarySpacing.screenHorizontal,
                end = DiarySpacing.screenHorizontal,
                top = DiarySpacing.md,
                bottom = DiarySpacing.sm
            )
        )

        // ── Loading state ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.isLoading && state.isEmpty,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Empty state ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = !state.isLoading && state.isEmpty,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(DiarySpacing.md))
                    Text(
                        text = "No activity yet",
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(DiarySpacing.xs))
                    Text(
                        text = "Likes and notes will show up here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Activity feed ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = !state.isEmpty,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = DiarySpacing.bottomNavHeight + DiarySpacing.lg
                )
            ) {
                // ── NEW section (today) ─────────────────────────────────
                val newItems = state.newItems
                if (newItems.isNotEmpty()) {
                    item(key = "header_new") {
                        SectionHeader(title = "NEW")
                    }
                    items(newItems, key = { "new_${it.id}" }) { item ->
                        ActivityItemRow(item = item)
                    }
                }

                // ── THIS WEEK section ───────────────────────────────────
                val weekItems = state.thisWeekItems
                if (weekItems.isNotEmpty()) {
                    item(key = "header_week") {
                        SectionHeader(
                            title = "THIS WEEK",
                            showTopDivider = newItems.isNotEmpty()
                        )
                    }
                    items(weekItems, key = { "week_${it.id}" }) { item ->
                        ActivityItemRow(item = item)
                    }
                }

                // ── EARLIER section ─────────────────────────────────────
                val earlierItems = state.earlierItems
                if (earlierItems.isNotEmpty()) {
                    item(key = "header_earlier") {
                        SectionHeader(
                            title = "EARLIER",
                            showTopDivider = newItems.isNotEmpty() || weekItems.isNotEmpty()
                        )
                    }
                    items(earlierItems, key = { "earlier_${it.id}" }) { item ->
                        ActivityItemRow(item = item)
                    }
                }
            }
        }
    }
}

// ── Section header ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    showTopDivider: Boolean = false
) {
    Column(
        modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)
    ) {
        if (showTopDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = DiarySpacing.sm)
            )
        }
        Text(
            text = title,
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(
                top = if (showTopDivider) 0.dp else DiarySpacing.xs,
                bottom = DiarySpacing.xs
            )
        )
    }
}

// ── Activity item row ───────────────────────────────────────────────────

@Composable
private fun ActivityItemRow(item: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DiarySpacing.screenHorizontal,
                vertical = DiarySpacing.sm
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular icon avatar
        ActivityIcon(iconType = item.iconType)

        Spacer(modifier = Modifier.width(DiarySpacing.sm))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                fontWeight = if (!item.isRead) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(DiarySpacing.xs))

        // Relative timestamp
        Text(
            text = ActivityViewModel.relativeTime(item.timestamp),
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        // Unread dot
        if (!item.isRead) {
            Spacer(modifier = Modifier.width(DiarySpacing.xs))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ── Circular icon for each activity type ────────────────────────────────

private data class IconConfig(
    val icon: ImageVector,
    val backgroundColor: Color,
    val tintColor: Color
)

@Composable
private fun ActivityIcon(iconType: ActivityIconType) {
    val config = when (iconType) {
        ActivityIconType.HEART -> IconConfig(
            icon = Icons.Default.Favorite,
            backgroundColor = Color(0xFF3D1520), // dark red
            tintColor = Color(0xFFF43F5E)        // rose red
        )
        ActivityIconType.ENVELOPE -> IconConfig(
            icon = Icons.Default.Email,
            backgroundColor = Color(0xFF1A2535), // dark blue
            tintColor = Color(0xFF60A5FA)        // sky blue
        )
        ActivityIconType.CHAT -> IconConfig(
            icon = Icons.Default.ChatBubble,
            backgroundColor = Color(0xFF1A2E1A), // dark green
            tintColor = Color(0xFF4ADE80)        // mint green
        )
        ActivityIconType.TROPHY -> IconConfig(
            icon = Icons.Default.EmojiEvents,
            backgroundColor = Color(0xFF2E2A1A), // dark amber
            tintColor = Color(0xFFFBBF24)        // amber gold
        )
        ActivityIconType.STAR -> IconConfig(
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF2A1A35), // dark purple
            tintColor = Color(0xFFA78BFA)        // soft purple
        )
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(config.backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = iconType.name,
            tint = config.tintColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
