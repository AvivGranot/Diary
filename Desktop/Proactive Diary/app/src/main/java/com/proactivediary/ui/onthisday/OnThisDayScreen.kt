package com.proactivediary.ui.onthisday

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.components.HeroSection
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun OnThisDayScreen(
    onBack: () -> Unit,
    onEntryTap: (String) -> Unit,
    viewModel: OnThisDayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Back button
        Row(
            modifier = Modifier.padding(horizontal = DiarySpacing.xs, vertical = DiarySpacing.xxs)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Looking through your memories\u2026",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (state.entries.isEmpty()) {
            // Empty state — Apple-style centered
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Timelapse,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(DiarySpacing.md))
                    Text(
                        text = "No memories for today",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(DiarySpacing.xs))
                    Text(
                        text = "Keep writing \u2014 your future self will thank you",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.md)
            ) {
                // Hero
                item {
                    HeroSection(
                        title = "Memories",
                        subtitle = state.todayFormatted
                    )
                }

                items(state.entries, key = { it.id }) { memory ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(100)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(800)) + slideInVertically(tween(600)) { 30 }
                    ) {
                        MemoryCard(
                            entry = memory,
                            onClick = { onEntryTap(memory.id) },
                            modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)
                        )
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    entry: MemoryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = LocalDiaryExtendedColors.current

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            // Time label — "2 years ago"
            Text(
                text = entry.dateFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(DiarySpacing.sm))

            // Hero image if available
            if (entry.images.isNotEmpty()) {
                val thumbFile = File(
                    context.filesDir,
                    "images/${entry.id}/thumbs/${entry.images.first().filename}"
                )
                if (thumbFile.exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = thumbFile,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                    }
                    Spacer(Modifier.height(DiarySpacing.sm))
                }
            }

            // Title
            if (entry.title.isNotBlank()) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(DiarySpacing.xxs))
            }

            // Preview text — quote style with serif italic
            Text(
                text = entry.preview,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Context chips
            val chips = buildList {
                entry.locationName?.let { add("\uD83D\uDCCD $it") }
                entry.weatherCondition?.let { condition ->
                    val temp = entry.weatherTemp?.let { "${it.toInt()}\u00B0" } ?: ""
                    add("$temp ${condition.replaceFirstChar { c -> c.uppercase() }}")
                }
            }

            if (chips.isNotEmpty()) {
                Spacer(Modifier.height(DiarySpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)) {
                    chips.forEach { chip ->
                        Text(
                            text = chip,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(DiarySpacing.xs))

            // Word count
            Text(
                text = "${entry.wordCount} words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
