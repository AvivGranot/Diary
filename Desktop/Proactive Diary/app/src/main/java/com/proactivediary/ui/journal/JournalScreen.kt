package com.proactivediary.ui.journal

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onEntryClick: (String) -> Unit = {},
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var entryToDelete by remember { mutableStateOf<DiaryCardData?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Pinned search bar
        SearchBar(
            query = state.searchQuery,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onClear = { viewModel.clearSearch() },
            modifier = Modifier.padding(top = 8.dp)
        )

        when {
            state.isLoading -> {
                // Loading state - blank
                Box(modifier = Modifier.fillMaxSize())
            }

            state.isEmpty && state.searchQuery.isBlank() -> {
                // Empty state - no entries at all
                EmptyState(
                    title = "Your writing starts here",
                    subtitle = "Begin your daily practice to see your entries"
                )
            }

            state.isSearchEmpty -> {
                // Empty search results
                EmptyState(
                    title = "Nothing found",
                    subtitle = "Try different words or check the date"
                )
            }

            else -> {
                // Entry list grouped by date
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // "On This Day" nostalgia card
                    if (state.onThisDay != null) {
                        item(key = "on_this_day") {
                            OnThisDayCard(
                                entry = state.onThisDay!!,
                                onClick = { onEntryClick(state.onThisDay!!.entryId) }
                            )
                        }
                    }

                    // Insights, heatmap, and mood trend at the top
                    item(key = "insights") {
                        InsightsCard(insights = state.insights)
                    }

                    item(key = "heatmap") {
                        CalendarHeatmap(writingDays = state.writingDays)
                    }

                    if (state.moodTrend.size >= 2) {
                        item(key = "mood_trend") {
                            MoodTrendChart(dataPoints = state.moodTrend)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (state.weeklyDigest.isVisible) {
                        item(key = "weekly_digest") {
                            WeeklyDigestCard(
                                digest = state.weeklyDigest,
                                onDismiss = { viewModel.dismissWeeklyDigest() }
                            )
                        }
                    }

                    state.groupedEntries.forEach { (dateGroup, entries) ->
                        // Date group header
                        item(key = "header_$dateGroup") {
                            Text(
                                text = dateGroup,
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.padding(
                                    top = 12.dp,
                                    bottom = 4.dp,
                                    start = 20.dp
                                )
                            )
                        }

                        // Entry cards with swipe-to-delete
                        items(
                            items = entries,
                            key = { it.id }
                        ) { cardData ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        entryToDelete = cardData
                                        false // Don't auto-dismiss; wait for confirmation
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                            Color(0xFFE57373).copy(alpha = 0.3f)
                                        else Color.Transparent,
                                        label = "swipe_bg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(end = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFD32F2F).copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                DiaryCard(
                                    data = cardData,
                                    onClick = { onEntryClick(cardData.id) }
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = "Delete entry?",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    text = "This entry will be permanently deleted.",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry.id)
                    entryToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OnThisDayCard(
    entry: OnThisDayEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = entry.label,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u201C${entry.firstLine}\u201D",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
