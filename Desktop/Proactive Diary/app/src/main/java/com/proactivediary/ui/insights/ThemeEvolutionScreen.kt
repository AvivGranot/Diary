package com.proactivediary.ui.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.proactivediary.ui.theme.CormorantGaramond

private val Cream = Color(0xFFF3EEE7)
private val Ink = Color(0xFF313131)
private val InkLight = Color(0xFF787878)
private val CardBg = Color(0xFFFAF7F2)
private val MoodGreat = Color(0xFF4A6B41)
private val MoodGood = Color(0xFF5B7A4A)
private val MoodOkay = Color(0xFF8B7A4A)
private val MoodBad = Color(0xFF8B5A4A)
private val MoodAwful = Color(0xFF7A3E3E)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeEvolutionScreen(
    onBack: () -> Unit,
    viewModel: ThemeEvolutionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .navigationBarsPadding()
    ) {
        // Header
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
                    tint = Ink
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Theme Evolution",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 22.sp,
                    color = Ink
                )
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Ink)
            }
            return
        }

        if (state.totalEntries == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(48.dp)
                ) {
                    Text(
                        text = "Not enough data yet",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            color = Ink
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Write more entries to see your patterns emerge",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = InkLight
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // ── OVERVIEW STRIP ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStat(
                    number = state.totalEntries.toString(),
                    label = "entries",
                    modifier = Modifier.weight(1f)
                )
                OverviewStat(
                    number = String.format(java.util.Locale.US, "%,d", state.totalWords),
                    label = "words",
                    modifier = Modifier.weight(1f)
                )
                OverviewStat(
                    number = String.format(java.util.Locale.US, "%.1f", state.averageMoodScore),
                    label = "avg mood",
                    modifier = Modifier.weight(1f)
                )
            }

            // Mood trend indicator
            val trendText = when (state.moodTrend) {
                "improving" -> "\u2197 Your mood is trending upward"
                "declining" -> "\u2198 Your mood has dipped recently"
                else -> "\u2192 Your mood has been steady"
            }
            Text(
                text = trendText,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── MOOD TIMELINE ───────────────────────────────────
            SectionHeader("MOOD OVER TIME")
            Spacer(Modifier.height(12.dp))

            if (state.moodTimeline.isNotEmpty()) {
                MoodTimelineChart(timeline = state.moodTimeline)
            }

            Spacer(Modifier.height(28.dp))

            // ── WRITING TIME PATTERNS ───────────────────────────
            SectionHeader("WHEN YOU WRITE")
            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBg
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.timePatterns.forEach { pattern ->
                        if (pattern.entryCount > 0) {
                            TimePatternRow(pattern = pattern, totalEntries = state.totalEntries)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── TOPIC CLOUD ─────────────────────────────────────
            if (state.topicWords.isNotEmpty()) {
                SectionHeader("WHAT YOU WRITE ABOUT")
                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg
                ) {
                    FlowRow(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.topicWords.forEach { topic ->
                            TopicWordChip(topic = topic)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            // ── LOCATION MOODS ──────────────────────────────────
            if (state.locationMoods.isNotEmpty()) {
                SectionHeader("WHERE YOU FEEL BEST")
                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        state.locationMoods.forEachIndexed { index, loc ->
                            LocationMoodRow(location = loc)
                            if (index < state.locationMoods.size - 1) {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── COMPONENTS ──────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = InkLight
        )
    )
}

@Composable
private fun OverviewStat(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                color = InkLight
            )
        )
    }
}

@Composable
private fun MoodTimelineChart(timeline: List<MoodDataPoint>) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mood scale labels on the left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "5 \u2014 Great",
                    style = TextStyle(fontSize = 9.sp, color = InkLight)
                )
                Text(
                    text = "1 \u2014 Awful",
                    style = TextStyle(fontSize = 9.sp, color = InkLight)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Horizontal scrolling bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                timeline.forEach { point ->
                    MoodBar(point = point)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoodLegendItem("Great", MoodGreat)
                MoodLegendItem("Good", MoodGood)
                MoodLegendItem("Okay", MoodOkay)
                MoodLegendItem("Bad", MoodBad)
                MoodLegendItem("Awful", MoodAwful)
            }
        }
    }
}

@Composable
private fun MoodBar(point: MoodDataPoint) {
    val targetHeight = point.averageMoodScore / 5f * 100f
    val barColor = moodToColor(point.dominantMood)

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val animatedHeight by animateFloatAsState(
        targetValue = if (appeared) targetHeight else 0f,
        animationSpec = tween(800),
        label = "barGrow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Entry count on top
        Text(
            text = point.entryCount.toString(),
            style = TextStyle(fontSize = 8.sp, color = InkLight)
        )

        Spacer(Modifier.height(2.dp))

        // Animated bar
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(animatedHeight.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(barColor)
        )

        Spacer(Modifier.height(4.dp))

        // Month label
        Text(
            text = point.month,
            style = TextStyle(fontSize = 9.sp, color = InkLight),
            maxLines = 1
        )
    }
}

@Composable
private fun MoodLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            style = TextStyle(fontSize = 8.sp, color = InkLight)
        )
    }
}

@Composable
private fun TimePatternRow(pattern: TimePattern, totalEntries: Int) {
    val emoji = when (pattern.label) {
        "Morning" -> "\u2600\uFE0F"  // ☀️
        "Afternoon" -> "\uD83C\uDF24\uFE0F" // 🌤️
        "Evening" -> "\uD83C\uDF05" // 🌅
        else -> "\uD83C\uDF19" // 🌙
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp
        )
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pattern.label,
                    style = TextStyle(fontSize = 14.sp, color = Ink)
                )
                Text(
                    text = "${pattern.entryCount} entries",
                    style = TextStyle(fontSize = 12.sp, color = InkLight)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Animated progress bar
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
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Ink.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFill)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (pattern.averageMoodScore >= 3.5f) MoodGood
                            else if (pattern.averageMoodScore >= 2.5f) MoodOkay
                            else MoodBad
                        )
                )
            }

            if (pattern.averageMoodScore > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "avg mood: ${String.format(java.util.Locale.US, "%.1f", pattern.averageMoodScore)}/5",
                    style = TextStyle(fontSize = 10.sp, color = InkLight)
                )
            }
        }
    }
}

@Composable
private fun TopicWordChip(topic: TopicWord) {
    // Size based on weight: small (10sp) to large (20sp)
    val fontSize = (10 + (topic.weight * 12)).sp
    val alpha = 0.4f + (topic.weight * 0.6f)

    Text(
        text = topic.word,
        style = TextStyle(
            fontFamily = CormorantGaramond,
            fontSize = fontSize,
            color = Ink.copy(alpha = alpha)
        ),
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
    )
}

@Composable
private fun LocationMoodRow(location: LocationMood) {
    val moodEmoji = when (location.dominantMood) {
        "great" -> "\uD83D\uDE0A"
        "good" -> "\uD83D\uDE42"
        "okay" -> "\uD83D\uDE10"
        "bad" -> "\uD83D\uDE1E"
        "awful" -> "\uD83D\uDE2D"
        else -> "\u2728"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = "\uD83D\uDCCD", // 📍
                fontSize = 14.sp
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = location.name,
                    style = TextStyle(fontSize = 14.sp, color = Ink),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${location.entryCount} entries \u2022 avg ${String.format(java.util.Locale.US, "%.1f", location.averageMoodScore)}/5",
                    style = TextStyle(fontSize = 11.sp, color = InkLight)
                )
            }
        }

        Text(
            text = moodEmoji,
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun moodToColor(mood: String): Color = when (mood) {
    "great" -> MoodGreat
    "good" -> MoodGood
    "okay" -> MoodOkay
    "bad" -> MoodBad
    "awful" -> MoodAwful
    else -> MoodOkay
}
