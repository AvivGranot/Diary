package com.proactivediary.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SimCardDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.components.HeroSection
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors

@Composable
fun PrivacyControlsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrivacyControlsViewModel = hiltViewModel()
) {
    val photosEnabled by viewModel.photosEnabled.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val calendarEnabled by viewModel.calendarEnabled.collectAsState()
    val fitnessEnabled by viewModel.fitnessEnabled.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val cloudSyncEnabled by viewModel.cloudSyncEnabled.collectAsState()
    val extendedColors = LocalDiaryExtendedColors.current

    // Trust shield pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val shieldScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shield_scale"
    )

    // Shield is green when fully private (cloud off)
    val shieldColor by animateColorAsState(
        targetValue = if (!cloudSyncEnabled) extendedColors.success else extendedColors.accent,
        animationSpec = tween(300),
        label = "shield_color"
    )

    val privacyLevel = if (!cloudSyncEnabled) "Maximum Privacy" else "Standard Privacy"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Back button ─────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = DiarySpacing.xs, vertical = DiarySpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }

        // ── Hero Section ────────────────────────────────────────────
        HeroSection(
            title = "Your journal is yours.\nPeriod.",
            subtitle = "We never see what you write. Everything stays on your device."
        )

        // ── Animated Trust Shield ───────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DiarySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Privacy shield",
                modifier = Modifier
                    .size(64.dp)
                    .scale(shieldScale),
                tint = shieldColor
            )
            Spacer(Modifier.height(DiarySpacing.xs))
            Text(
                text = privacyLevel,
                style = MaterialTheme.typography.titleSmall,
                color = shieldColor
            )
        }

        Spacer(Modifier.height(DiarySpacing.md))

        // ── Card 1: Where your words live ───────────────────────────
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "Where your words live",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DiarySpacing.xs)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                PrivacyToggleRow(
                    icon = Icons.Outlined.Cloud,
                    title = "Cloud Backup",
                    description = "Sync to the cloud for backup. Disable to keep everything local-only.",
                    checked = cloudSyncEnabled,
                    onCheckedChange = { viewModel.toggleCloudSync(it) }
                )
            }
        }

        Spacer(Modifier.height(DiarySpacing.sectionGap))

        // ── Card 2: What inspires your prompts ──────────────────────
        Column(modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)) {
            Text(
                text = "What inspires your prompts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DiarySpacing.xs)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                PrivacyToggleRow(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "Photos",
                    description = "Recent photos can inspire entries",
                    checked = photosEnabled,
                    onCheckedChange = { viewModel.togglePhotos(it) }
                )
                Spacer(Modifier.height(DiarySpacing.sm))
                PrivacyToggleRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Location",
                    description = "Places you visit suggest writing prompts",
                    checked = locationEnabled,
                    onCheckedChange = { viewModel.toggleLocation(it) }
                )
                Spacer(Modifier.height(DiarySpacing.sm))
                PrivacyToggleRow(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Calendar",
                    description = "Events from your calendar",
                    checked = calendarEnabled,
                    onCheckedChange = { viewModel.toggleCalendar(it) }
                )
                Spacer(Modifier.height(DiarySpacing.sm))
                PrivacyToggleRow(
                    icon = Icons.Outlined.FitnessCenter,
                    title = "Fitness",
                    description = "Workouts and activity data",
                    checked = fitnessEnabled,
                    onCheckedChange = { viewModel.toggleFitness(it) }
                )
                Spacer(Modifier.height(DiarySpacing.sm))
                PrivacyToggleRow(
                    icon = Icons.Outlined.MusicNote,
                    title = "Music",
                    description = "Recently played music",
                    checked = musicEnabled,
                    onCheckedChange = { viewModel.toggleMusic(it) }
                )
            }
        }

        Spacer(Modifier.height(DiarySpacing.sectionGap))

        // ── Trust badges (horizontal scroll) ────────────────────────
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DiarySpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
        ) {
            TrustBadge(icon = Icons.Outlined.Shield, text = "Local-first storage")
            TrustBadge(icon = Icons.Outlined.Lock, text = "Encrypted")
            TrustBadge(icon = Icons.Outlined.SimCardDownload, text = "Always exportable")
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun PrivacyToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val extendedColors = LocalDiaryExtendedColors.current
    val iconTint by animateColorAsState(
        targetValue = if (checked) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "toggle_icon_tint"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Spacer(Modifier.width(DiarySpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = extendedColors.accent,
                checkedTrackColor = extendedColors.accent.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun TrustBadge(
    icon: ImageVector,
    text: String
) {
    GlassCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
