package com.proactivediary.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.journal.DiaryCard
import com.proactivediary.ui.journal.DiaryCardData
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PlusJakartaSans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * V3 Diary home tab -- date header, today's prompt card, date-grouped entries,
 * AI insight card at the bottom.
 */
@Composable
fun DiaryHomeScreen(
    onSearchClick: () -> Unit = {},
    onWriteClick: () -> Unit = {},
    onQuickCapture: () -> Unit = {},
    onEntryClick: (String) -> Unit = {},
    onEditEntry: (String) -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    viewModel: DiaryHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val extendedColors = LocalDiaryExtendedColors.current
    val context = LocalContext.current
    var entryToDelete by remember { mutableStateOf<DiaryCardData?>(null) }
    var contextMenuEntry by remember { mutableStateOf<DiaryCardData?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar: date left, search + write icons right ──────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DiarySpacing.screenHorizontal,
                    vertical = DiarySpacing.sm
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
                ),
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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

        // ── Streak banner ────────────────────────────────────────────────
        uiState.streakMessage?.let { message ->
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = DiarySpacing.screenHorizontal,
                        vertical = DiarySpacing.xs
                    )
            )
        }

        // ── Scrollable content ──────────────────────────────────────────
        if (uiState.isLoading) {
            // Skeleton placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DiarySpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                Spacer(modifier = Modifier.height(DiarySpacing.md))
                repeat(4) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                    ) {}
                }
            }
        } else if (uiState.isEmpty) {
            // Empty state — elevated to top third, not dead center
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DiarySpacing.screenHorizontal),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 120.dp)
                ) {
                    Text(
                        text = "Your diary awaits",
                        style = TextStyle(
                            fontFamily = InstrumentSerif,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(DiarySpacing.xs))
                    Text(
                        text = "Start writing — your story matters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(DiarySpacing.lg))
                    Button(
                        onClick = onWriteClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.accent,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Write what\u2019s been on your mind",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = DiarySpacing.xs,
                    bottom = 96.dp // clearance for bottom nav
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Today's Prompt card ─────────────────────────────────
                item(key = "todays_prompt") {
                    TodaysPromptCard(
                        prompt = uiState.todayPrompt,
                        onWriteNow = onWriteClick,
                        onQuickCapture = onQuickCapture,
                        modifier = Modifier.padding(
                            horizontal = DiarySpacing.screenHorizontal,
                            vertical = DiarySpacing.xs
                        )
                    )
                }

                // ── Date-grouped entries ────────────────────────────────
                uiState.groupedEntries.forEach { (dateGroup, entries) ->
                    // Section header
                    item(key = "header_$dateGroup") {
                        Text(
                            text = dateGroup,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(
                                start = DiarySpacing.screenHorizontal,
                                top = DiarySpacing.lg,
                                bottom = DiarySpacing.xs
                            )
                        )
                    }

                    // Entry cards with swipe-to-delete + long-press menu
                    items(
                        items = entries,
                        key = { it.id }
                    ) { cardData ->
                        val onClick = remember(cardData.id) { { onEntryClick(cardData.id) } }
                        val visible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible.value = true }

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    entryToDelete = cardData
                                    false
                                } else false
                            }
                        )

                        AnimatedVisibility(
                            visible = visible.value,
                            enter = fadeIn(tween(150)) + slideInVertically(tween(200)) { it / 4 }
                        ) {
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
                                modifier = Modifier.padding(
                                    horizontal = DiarySpacing.md,
                                    vertical = 4.dp
                                )
                            ) {
                                Box {
                                    DiaryCard(
                                        data = cardData,
                                        onClick = onClick,
                                        onLongClick = { contextMenuEntry = cardData }
                                    )
                                    DropdownMenu(
                                        expanded = contextMenuEntry?.id == cardData.id,
                                        onDismissRequest = { contextMenuEntry = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                            onClick = {
                                                val id = contextMenuEntry?.id ?: return@DropdownMenuItem
                                                contextMenuEntry = null
                                                onEditEntry(id)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Share") },
                                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                            onClick = {
                                                val entry = contextMenuEntry ?: return@DropdownMenuItem
                                                contextMenuEntry = null
                                                val text = buildString {
                                                    if (entry.title.isNotBlank()) {
                                                        appendLine(entry.title)
                                                        appendLine()
                                                    }
                                                    append(entry.content.take(500))
                                                }
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    putExtra(Intent.EXTRA_TEXT, text)
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, null))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = Color(0xFFD32F2F)) },
                                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) },
                                            onClick = {
                                                entryToDelete = contextMenuEntry
                                                contextMenuEntry = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── View all link ───────────────────────────────────────
                val totalEntries = uiState.groupedEntries.values.sumOf { it.size }
                if (totalEntries > 20) {
                    item(key = "view_all") {
                        Text(
                            text = "View all entries",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = extendedColors.accent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToJournal() }
                                .padding(
                                    horizontal = DiarySpacing.screenHorizontal,
                                    vertical = DiarySpacing.md
                                )
                        )
                    }
                }

                // ── AI Insight card ─────────────────────────────────────
                if (uiState.aiInsight.isAvailable) {
                    item(key = "ai_insight") {
                        AiInsightHomeCard(
                            summary = uiState.aiInsight.summary,
                            modifier = Modifier.padding(
                                horizontal = DiarySpacing.screenHorizontal,
                                vertical = DiarySpacing.sm
                            )
                        )
                    }
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

// ── Today's Prompt Card ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodaysPromptCard(
    prompt: String,
    onWriteNow: () -> Unit,
    onQuickCapture: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(DiarySpacing.md)
        ) {
            // Label row: sparkle + "TODAY'S PROMPT"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = extendedColors.accent
                )
                Text(
                    text = "TODAY'S PROMPT",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = extendedColors.accent
                    )
                )
            }

            Spacer(modifier = Modifier.height(DiarySpacing.sm))

            // Prompt question text in serif
            Text(
                text = prompt,
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(DiarySpacing.md))

            // CTA buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWriteNow,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.accent,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Write now",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Surface(
                    onClick = onQuickCapture,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Quick thought",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

// ── AI Insight Home Card ────────────────────────────────────────────────────
@Composable
private fun AiInsightHomeCard(
    summary: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalDiaryExtendedColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Blue left accent border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                extendedColors.accent,
                                extendedColors.accent.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(DiarySpacing.md)
            ) {
                // "AI INSIGHT" label
                Text(
                    text = "AI INSIGHT",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = extendedColors.accent
                    )
                )

                Spacer(modifier = Modifier.height(DiarySpacing.xs))

                // Insight summary text
                Text(
                    text = summary,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
