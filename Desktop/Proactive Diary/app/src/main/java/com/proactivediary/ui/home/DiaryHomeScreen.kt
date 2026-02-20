package com.proactivediary.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.journal.DiaryCardData
import com.proactivediary.ui.journal.JournalViewModel
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Diary home tab — shows recent entries with search and write icons in top-right.
 * No "My Diary" header, no streak, no mood colors.
 */
@Composable
fun DiaryHomeScreen(
    onSearchClick: () -> Unit = {},
    onWriteClick: () -> Unit = {},
    onEntryClick: (String) -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    journalViewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by journalViewModel.uiState.collectAsState()
    val entries = uiState.entries
    val extendedColors = LocalDiaryExtendedColors.current
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top bar — today's date + search + write icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DiarySpacing.screenHorizontal, vertical = DiarySpacing.sm),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onWriteClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Write",
                    tint = extendedColors.accent
                )
            }
        }

        // Entry list
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DiarySpacing.screenHorizontal),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your diary awaits",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(DiarySpacing.xs))
                    Text(
                        text = "Tap the pencil to write your first entry",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = DiarySpacing.screenHorizontal,
                    vertical = DiarySpacing.xs
                ),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                items(
                    items = entries.take(30),
                    key = { entry: DiaryCardData -> entry.id }
                ) { entry: DiaryCardData ->
                    DiaryEntryRow(
                        entry = entry,
                        dateFormat = dateFormat,
                        onClick = { onEntryClick(entry.id) }
                    )
                }

                // View all link
                if (entries.size > 30) {
                    item {
                        Text(
                            text = "View all entries",
                            style = MaterialTheme.typography.labelLarge,
                            color = extendedColors.accent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToJournal() }
                                .padding(vertical = DiarySpacing.md)
                        )
                    }
                }

                // Bottom spacing for nav bar
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DiaryEntryRow(
    entry: DiaryCardData,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(DiarySpacing.cardPadding)
    ) {
        // Mint green side line
        Box(
            modifier = Modifier
                .width(DiarySpacing.sideLineWidth)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(extendedColors.cardSideLine)
        )

        Spacer(modifier = Modifier.width(DiarySpacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(DiarySpacing.xs))

        // Date
        Text(
            text = dateFormat.format(Date(entry.createdAt)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
