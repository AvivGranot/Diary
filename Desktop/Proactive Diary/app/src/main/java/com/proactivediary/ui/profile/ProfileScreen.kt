package com.proactivediary.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors

/**
 * Profile tab — avatar, total likes, recap cards, settings navigation.
 * Replaces the old SettingsScreen as a tab.
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToLayout: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToThemeEvolution: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        // Top bar with settings gear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal, vertical = DiarySpacing.sm),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Avatar + name section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(DiarySpacing.avatarSizeLarge)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.lg))

        // Stats row — total entries, total likes, days active
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "0", label = "Entries")
            StatItem(value = "0", label = "Likes")
            StatItem(value = "0", label = "Days")
        }

        Spacer(modifier = Modifier.height(DiarySpacing.xl))

        // Menu items
        Column(
            modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiarySpacing.xxs)
        ) {
            ProfileMenuItem(
                icon = Icons.Outlined.Star,
                label = "Goals",
                onClick = onNavigateToGoals
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Timeline,
                label = "Theme Evolution",
                onClick = onNavigateToThemeEvolution
            )
            ProfileMenuItem(
                icon = Icons.Outlined.GridView,
                label = "Layout",
                onClick = onNavigateToLayout
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Download,
                label = "Export Data",
                onClick = onNavigateToExport
            )
            ProfileMenuItem(
                icon = Icons.Outlined.ContactSupport,
                label = "Contact Support",
                onClick = onNavigateToSupport
            )
        }

        // Bottom spacing for nav bar
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = DiarySpacing.md, vertical = DiarySpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = extendedColors.accent
        )
        Spacer(modifier = Modifier.width(DiarySpacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
