package com.proactivediary.ui.export

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun YearInReviewScreen(
    onBack: () -> Unit,
    isPremium: Boolean,
    onShowPaywall: () -> Unit,
    viewModel: BookExportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header: back arrow + title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Year in Review",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // Year selector chips (if multiple years available)
        if (state.availableYears.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableYears.forEach { year ->
                    FilterChip(
                        selected = year == state.year,
                        onClick = { viewModel.selectYear(year) },
                        label = {
                            Text(
                                text = year.toString(),
                                style = TextStyle(fontSize = 13.sp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                            selectedLabelColor = MaterialTheme.colorScheme.surface,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // YOUR YEAR section header
        SectionHeader("YOUR YEAR")
        Spacer(Modifier.height(8.dp))

        // Stats card
        StatsCard(state)

        Spacer(Modifier.height(24.dp))

        // Generate Book button
        if (state.totalEntries > 0) {
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            if (state.exportState !is ExportState.Generating) {
                                viewModel.generateBook()
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GENERATE BOOK",
                        style = TextStyle(
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onShowPaywall() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Pro",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "GENERATE BOOK",
                            style = TextStyle(
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "A beautiful PDF saved to Downloads",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Empty state
            Text(
                text = "No entries for ${state.year}",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Export state feedback
        when (val exportState = state.exportState) {
            is ExportState.Generating -> {
                LinearProgressIndicator(
                    progress = { exportState.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(exportState.progress * 100).toInt()}%",
                    style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                )
            }
            is ExportState.Success -> {
                Text(
                    text = exportState.message,
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                )
            }
            is ExportState.Error -> {
                Text(
                    text = exportState.message,
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                )
            }
            else -> {}
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 11.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
private fun StatsCard(state: BookExportUiState) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Total entries — large number
            Text(
                text = state.totalEntries.toString(),
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "entries",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Total words — large number
            Text(
                text = formatDisplayNumber(state.totalWords),
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "words",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(Modifier.height(20.dp))

            // Row: avg words | longest streak | most common mood
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    number = state.averageWordsPerEntry.toString(),
                    label = "avg words/entry",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    number = state.longestStreak.toString(),
                    label = "longest streak",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    number = state.mostCommonMood ?: "\u2014",
                    label = "top mood",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Months summary
            Text(
                text = "You wrote in ${state.monthsWithEntries} month${if (state.monthsWithEntries != 1) "s" else ""}",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
private fun StatItem(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

private fun formatDisplayNumber(n: Int): String {
    return String.format(java.util.Locale.US, "%,d", n)
}
