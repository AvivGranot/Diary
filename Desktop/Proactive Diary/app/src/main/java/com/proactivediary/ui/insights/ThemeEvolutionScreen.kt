package com.proactivediary.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import com.proactivediary.ui.theme.InstrumentSerif

private enum class EvolutionPage {
    OVERVIEW, WRITING_TIME, TOPICS, LOCATIONS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeEvolutionScreen(
    onBack: () -> Unit,
    viewModel: ThemeEvolutionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Loading state
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Preparing\u2026",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        return
    }

    // Empty state
    if (state.totalEntries == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Not enough data yet",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Every entry adds a brushstroke.\nStart writing and watch your patterns unfold.",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "\u2190 Back",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.clickable(onClick = onBack)
                )
            }
        }
        return
    }

    // Build dynamic page list based on available data
    val pages = buildList {
        add(EvolutionPage.OVERVIEW)
        if (state.timePatterns.any { it.entryCount > 0 }) add(EvolutionPage.WRITING_TIME)
        if (state.topicWords.isNotEmpty()) add(EvolutionPage.TOPICS)
        if (state.locationMoods.isNotEmpty()) add(EvolutionPage.LOCATIONS)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Theme Evolution",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        // Swipeable pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) { pageIndex ->
            when (pages[pageIndex]) {
                EvolutionPage.OVERVIEW -> OverviewPage(state = state)
                EvolutionPage.WRITING_TIME -> WritingTimePage(
                    patterns = state.timePatterns,
                    totalEntries = state.totalEntries
                )
                EvolutionPage.TOPICS -> TopicsPage(topics = state.topicWords)
                EvolutionPage.LOCATIONS -> LocationsPage(locations = state.locationMoods)
            }
        }

        // Instagram Stories-style progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
                ) {
                    if (index <= pagerState.currentPage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(1.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }

        // Swipe hint on first page
        if (pagerState.currentPage == 0) {
            Text(
                text = "Swipe to explore \u2192",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── PAGE 1: OVERVIEW ─────────────────────────────────────────

@Composable
private fun OverviewPage(state: ThemeEvolutionUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your Patterns",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Total entries — big number
            Text(
                text = state.totalEntries.toString(),
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "entries",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(24.dp))

            // Total words
            Text(
                text = String.format(java.util.Locale.US, "%,d", state.totalWords),
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "words written",
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

// ── PAGE 2: WRITING TIME ─────────────────────────────────────

@Composable
private fun WritingTimePage(patterns: List<TimePattern>, totalEntries: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "When You Write",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                patterns.forEach { pattern ->
                    if (pattern.entryCount > 0) {
                        TimePatternRow(pattern = pattern, totalEntries = totalEntries)
                    }
                }
            }
        }
    }
}

// ── PAGE 3: TOPICS ──────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicsPage(topics: List<TopicWord>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What You Write About",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topics.forEach { topic ->
                    TopicWordChip(topic = topic)
                }
            }
        }
    }
}

// ── PAGE 4: LOCATIONS ───────────────────────────────────────

@Composable
private fun LocationsPage(locations: List<LocationMood>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Where You Write",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                locations.forEach { loc ->
                    LocationMoodRow(location = loc)
                }
            }
        }
    }
}

// ── SHARED COMPONENTS ──────────────────────────────────────

@Composable
private fun TimePatternRow(pattern: TimePattern, totalEntries: Int) {
    val emoji = when (pattern.label) {
        "Morning" -> "\u2600\uFE0F"
        "Afternoon" -> "\uD83C\uDF24\uFE0F"
        "Evening" -> "\uD83C\uDF05"
        else -> "\uD83C\uDF19"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pattern.label,
                    style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = "${pattern.entryCount} entries",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(Modifier.height(6.dp))

            var appeared by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { appeared = true }
            val animatedFill by animateFloatAsState(
                targetValue = if (appeared) pattern.percentage.coerceIn(0f, 1f) else 0f,
                animationSpec = tween(800),
                label = "progressFill"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFill)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f))
                )
            }
        }
    }
}

@Composable
private fun TopicWordChip(topic: TopicWord) {
    val fontSize = (12 + (topic.weight * 14)).sp
    val alpha = 0.4f + (topic.weight * 0.6f)

    Text(
        text = topic.word,
        style = TextStyle(
            fontFamily = InstrumentSerif,
            fontSize = fontSize,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
        ),
        modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
    )
}

@Composable
private fun LocationMoodRow(location: LocationMood) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDCCD", fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = location.name,
                style = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${location.entryCount} entries",
                style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

