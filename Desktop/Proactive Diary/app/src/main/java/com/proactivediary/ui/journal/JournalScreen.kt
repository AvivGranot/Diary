package com.proactivediary.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.components.GlassCard
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.proactivediary.data.db.entities.JournalEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onEntryClick: (String) -> Unit = {},
    onNavigateToWrite: (() -> Unit)? = null,
    onNavigateToOnThisDay: (() -> Unit)? = null,
    onNavigateToRecentlyDeleted: (() -> Unit)? = null,
    onNavigateToMap: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    viewModel: JournalViewModel = hiltViewModel(),
    journalManageVM: JournalManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val journalManageState by journalManageVM.uiState.collectAsState()
    var entryToDelete by remember { mutableStateOf<DiaryCardData?>(null) }
    var viewMode by remember { mutableStateOf("list") } // "list", "calendar", "gallery"
    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var showSidebar by remember { mutableStateOf(false) }
    var showCreateJournal by remember { mutableStateOf(false) }
    var editingJournal by remember { mutableStateOf<JournalEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (onBack != null) Modifier.pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                            if (totalDrag > 200f) {
                                onBack()
                                totalDrag = 0f
                            }
                        }
                    )
                } else Modifier
            )
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back button header when shown as standalone screen
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Your Journal",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        // Sidebar toggle + Pinned search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showSidebar = !showSidebar }) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = "Journals",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SearchBar(
                query = state.searchQuery,
                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onClear = { viewModel.clearSearch() },
                modifier = Modifier.weight(1f)
            )
        }

        // View mode switcher
        if (!state.isLoading && !state.isEmpty) {
            ViewModeSwitcher(
                selectedMode = viewMode,
                onModeSelected = { mode ->
                    viewMode = mode
                    selectedDate = null
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when {
            state.isLoading -> {
                // Skeleton loading state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(4) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                        ) {}
                    }
                }
            }

            state.isEmpty && state.searchQuery.isBlank() -> {
                // Empty state - no entries at all
                EmptyState(
                    title = "This is yours.",
                    subtitle = "Your first entry is waiting.",
                    actionLabel = if (onNavigateToWrite != null) "Begin" else null,
                    onAction = onNavigateToWrite
                )
            }

            state.isSearchEmpty -> {
                // Empty search results — Apple Spotlight style
                EmptyState(
                    title = "No results for \u201C${state.searchQuery}\u201D",
                    subtitle = "Try different words"
                )
            }

            else -> {
                when (viewMode) {
                    "map" -> {
                        // Navigate to places map screen
                        LaunchedEffect(Unit) {
                            onNavigateToMap?.invoke()
                        }
                    }
                    "gallery" -> {
                        GalleryView(
                            onImageClick = { entryId -> onEntryClick(entryId) }
                        )
                    }
                    "calendar" -> {
                        Column {
                            CalendarView(
                                onDaySelected = { date -> selectedDate = date }
                            )

                            // Show filtered entries below calendar
                            val listState = rememberLazyListState()
                            val filteredEntries = if (selectedDate != null) {
                                val zone = java.time.ZoneId.systemDefault()
                                val dayStart = selectedDate!!.atStartOfDay(zone).toInstant().toEpochMilli()
                                val dayEnd = selectedDate!!.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                                state.entries.filter { it.createdAt in dayStart..dayEnd }
                            } else {
                                state.entries
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (selectedDate != null) {
                                    item {
                                        Text(
                                            text = "Entries for ${selectedDate!!.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                                            style = TextStyle(
                                                fontFamily = InstrumentSerif,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                items(filteredEntries, key = { it.id }) { cardData ->
                                    val onClick = remember(cardData.id) { { onEntryClick(cardData.id) } }
                                    DiaryCard(
                                        data = cardData,
                                        onClick = onClick
                                    )
                                }

                                if (filteredEntries.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No entries for this day",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Default,
                                                fontSize = 14.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                            ),
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // List view
                        val listState = rememberLazyListState()

                        // Trigger loadMore when near the end of the list
                        LaunchedEffect(listState) {
                            snapshotFlow {
                                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                val totalItems = listState.layoutInfo.totalItemsCount
                                lastVisible >= totalItems - 5
                            }.distinctUntilChanged().collect { nearEnd ->
                                if (nearEnd && state.hasMoreEntries && !state.isLoadingMore) {
                                    viewModel.loadMoreEntries()
                                }
                            }
                        }

                        // Entry list grouped by date
                        LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // "On This Day" nostalgia card
                    if (state.onThisDay != null) {
                        item(key = "on_this_day") {
                            OnThisDayCard(
                                entry = state.onThisDay!!,
                                onClick = { onEntryClick(state.onThisDay!!.entryId) },
                                onSeeAll = onNavigateToOnThisDay
                            )
                        }
                    }

                    // Insights and heatmap at the top
                    item(key = "insights") {
                        InsightsCard(insights = state.insights)
                    }

                    item(key = "heatmap") {
                        CalendarHeatmap(writingDays = state.writingDays)
                    }

                    if (state.weeklyDigest.isVisible) {
                        item(key = "weekly_digest") {
                            WeeklyDigestCard(
                                digest = state.weeklyDigest,
                                onDismiss = { viewModel.dismissWeeklyDigest() }
                            )
                        }
                    }

                    // AI Insight card
                    if (state.aiInsight.isAvailable || state.aiInsight.isLocked) {
                        item(key = "ai_insight") {
                            AIInsightCard(data = state.aiInsight)
                        }
                    }

                    state.groupedEntries.forEach { (dateGroup, entries) ->
                        // Date group header
                        item(key = "header_$dateGroup") {
                            Text(
                                text = dateGroup,
                                style = TextStyle(
                                    fontFamily = InstrumentSerif,
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
                                val cardOnClick = remember(cardData.id) { { onEntryClick(cardData.id) } }
                                DiaryCard(
                                    data = cardData,
                                    onClick = cardOnClick
                                )
                            }
                        }
                    }

                            // Bottom spacing (clearance for persistent bottom nav)
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    // Sidebar scrim
    if (showSidebar) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showSidebar = false }
        )
    }

    // Sidebar — slides in from left
    AnimatedVisibility(
        visible = showSidebar,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        JournalSidebar(
            journals = journalManageState.journals,
            selectedJournalId = journalManageState.selectedJournalId,
            onJournalSelected = { id ->
                journalManageVM.selectJournal(id)
                viewModel.filterByJournal(id)
                showSidebar = false
            },
            onCreateJournal = {
                editingJournal = null
                showCreateJournal = true
            },
            onEditJournal = { journal ->
                editingJournal = journal
                showCreateJournal = true
            },
            onClose = { showSidebar = false },
            onReorder = { from, to ->
                journalManageVM.reorderJournals(from, to)
            }
        )
    }

    } // Close swipe-back Box

    // Create/Edit journal sheet
    if (showCreateJournal) {
        CreateJournalSheet(
            onDismiss = {
                showCreateJournal = false
                editingJournal = null
            },
            onSave = { title, emoji, colorKey ->
                if (editingJournal != null) {
                    journalManageVM.updateJournal(
                        editingJournal!!.copy(
                            title = title,
                            emoji = emoji,
                            colorKey = colorKey
                        )
                    )
                } else {
                    journalManageVM.createJournal(title, emoji, colorKey)
                }
                showCreateJournal = false
                editingJournal = null
            },
            existingJournal = editingJournal,
            onDelete = if (editingJournal != null) {
                {
                    journalManageVM.deleteJournal(editingJournal!!.id)
                    showCreateJournal = false
                    editingJournal = null
                }
            } else null
        )
    }

    // Delete confirmation dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = "Delete entry?",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    text = "This entry will be moved to Recently Deleted and permanently removed after 30 days.",
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
    onSeeAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DiarySpacing.screenHorizontal, vertical = 4.dp),
        onClick = onClick
    ) {
        Column {
            // Header row: timelapse icon + label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DiarySpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timelapse,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = extendedColors.accent
                )
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            // Quote-style preview
            Text(
                text = "\u201C${entry.firstLine}\u201D",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (onSeeAll != null) {
                Spacer(modifier = Modifier.height(DiarySpacing.sm))
                Text(
                    text = "Open memories",
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.accent,
                    modifier = Modifier.clickable(onClick = onSeeAll)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal)
        ) {
            // Search icon for empty search results
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.height(DiarySpacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(DiarySpacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(DiarySpacing.lg))
                Surface(
                    modifier = Modifier.clickable(onClick = onAction),
                    shape = com.proactivediary.ui.theme.PillShape,
                    color = MaterialTheme.colorScheme.onBackground
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeSwitcher(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ViewModeButton(
            label = "List",
            isSelected = selectedMode == "list",
            onClick = { onModeSelected("list") }
        )
        ViewModeButton(
            label = "Calendar",
            isSelected = selectedMode == "calendar",
            onClick = { onModeSelected("calendar") }
        )
        ViewModeButton(
            label = "Gallery",
            isSelected = selectedMode == "gallery",
            onClick = { onModeSelected("gallery") }
        )
        ViewModeButton(
            label = "Map",
            isSelected = selectedMode == "map",
            onClick = { onModeSelected("map") }
        )
    }
}

@Composable
private fun ViewModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Underline-based selection — consistent with Discover's CategoryChip pattern
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(1.5.dp)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}
