package com.proactivediary.ui.journal

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun JournalScreen(
    onEntryClick: (String) -> Unit = {},
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                    title = "Your diary is empty",
                    subtitle = "Start writing to see your entries here"
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    start = 4.dp
                                )
                            )
                        }

                        // Entry cards
                        items(
                            items = entries,
                            key = { it.id }
                        ) { cardData ->
                            DiaryCard(
                                data = cardData,
                                onClick = { onEntryClick(cardData.id) }
                            )
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
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
