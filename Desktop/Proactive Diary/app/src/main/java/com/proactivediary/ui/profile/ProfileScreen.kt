package com.proactivediary.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PlusJakartaSans

/**
 * Profile tab -- V3 wireframe design.
 * Serif display name, @handle, stats row, Spotify-style gradient recap cards, menu items.
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
    onNavigateToSubscription: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = LocalDiaryExtendedColors.current
    val scrollState = rememberScrollState()

    // Photo picker for profile image upload
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadProfilePhoto(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
    ) {
        // Top spacing
        Spacer(modifier = Modifier.height(DiarySpacing.sm))

        // ── Avatar + Name + Handle ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture — clickable to change
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.photoUrl != null) {
                    AsyncImage(
                        model = state.photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Initial-letter avatar (not generic person icon)
                    val initial = state.displayName.firstOrNull()?.uppercase() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(extendedColors.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = InstrumentSerif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 28.sp
                            ),
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Camera icon overlay (bottom-right)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Change photo",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(DiarySpacing.xs))

            // Display name in Instrument Serif
            Text(
                text = state.displayName.ifBlank { "Writer" },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // @handle
            if (state.handle.isNotBlank()) {
                Text(
                    text = state.handle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PlusJakartaSans
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(DiarySpacing.md))

        // ── Stats row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "${state.totalEntries}",
                label = "entries"
            )
            StatItem(
                value = "${state.totalLikesReceived}",
                label = "likes",
                valueColor = Color(0xFFEF4444) // Red for likes
            )
            StatItem(
                value = "${state.totalQuotes}",
                label = "quotes"
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.md))

        // ── Recap cards (2x2 grid) ──────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
        ) {
            // Row 1: Entries this year + Total likes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
            ) {
                RecapCard(
                    modifier = Modifier.weight(1f),
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF34D399))
                    ),
                    bigText = "${state.entriesThisYear}",
                    label = "Entries this year",
                    subtitle = if (state.entriesThisYear > 20) "Top 5% of writers"
                    else if (state.entriesThisYear > 5) "Top 20% of writers"
                    else "Keep going!"
                )
                RecapCard(
                    modifier = Modifier.weight(1f),
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF9F1239), Color(0xFFE11D48), Color(0xFFFB7185))
                    ),
                    bigText = "${state.totalLikesReceived}",
                    label = "Total likes received",
                    subtitle = if (state.totalLikesReceived > 0) "People love your words"
                    else "Share your first quote"
                )
            }

            // Row 2: Words written + Best writing day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
            ) {
                RecapCard(
                    modifier = Modifier.weight(1f),
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFBBF24))
                    ),
                    bigText = formatWordCount(state.totalWords),
                    label = "Words written",
                    subtitle = when {
                        state.totalWords >= 50_000 -> "That's a short novel"
                        state.totalWords >= 10_000 -> "That's a novella"
                        state.totalWords >= 1_000 -> "That's a short story"
                        else -> "Every word counts"
                    }
                )
                RecapCard(
                    modifier = Modifier.weight(1f),
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1E3A5F), Color(0xFF3B82F6), Color(0xFF93C5FD))
                    ),
                    bigText = state.bestWritingDay.take(3).ifBlank { "--" },
                    label = "Best writing day",
                    subtitle = if (state.bestWritingDay.isNotBlank())
                        "You wrote ${state.bestWritingDayPercent}% of entries on ${state.bestWritingDay}s"
                    else "Write more to discover"
                )
            }
        }

        Spacer(modifier = Modifier.height(DiarySpacing.md))

        // ── Menu items ──────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiarySpacing.xxs)
        ) {
            ProfileMenuItem(
                icon = Icons.Outlined.Star,
                label = "Goals & Reminders",
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
            ProfileMenuItem(
                icon = Icons.Outlined.Settings,
                label = "Account & Privacy",
                onClick = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.height(DiarySpacing.sm))

        // ── Sign Out ──────────────────────────────────────────────────
        if (state.isSignedIn) {
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSans,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DiarySpacing.screenHorizontal)
                    .clickable(onClick = onSignOut)
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }

        // Bottom spacing for nav bar
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Stats item ──────────────────────────────────────────────────────────

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PlusJakartaSans
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Spotify-style gradient recap card ───────────────────────────────────

@Composable
private fun RecapCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    bigText: String,
    label: String,
    subtitle: String
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(gradient)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: big number
            Text(
                text = bigText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = InstrumentSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp
                ),
                color = Color.White
            )

            // Bottom: label + subtitle
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ── Menu item row ───────────────────────────────────────────────────────

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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = DiarySpacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = extendedColors.accent
        )
        Spacer(modifier = Modifier.width(DiarySpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = PlusJakartaSans
            ),
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

// ── Helpers ─────────────────────────────────────────────────────────────

private fun formatWordCount(words: Int): String {
    return when {
        words >= 1_000_000 -> "${words / 1_000_000}.${(words % 1_000_000) / 100_000}M"
        words >= 1_000 -> "${words / 1_000}.${(words % 1_000) / 100}k"
        else -> "$words"
    }
}
