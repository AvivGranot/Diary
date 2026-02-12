package com.proactivediary.ui.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.domain.suggestions.Suggestion
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsBottomSheet(
    onDismiss: () -> Unit,
    onSuggestionSelected: (Suggestion) -> Unit,
    viewModel: SuggestionsViewModel = hiltViewModel(),
    textColor: Color = DiaryColors.Ink,
    secondaryTextColor: Color = DiaryColors.Pencil,
    backgroundColor: Color = DiaryColors.Paper
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = backgroundColor,
        dragHandle = {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(secondaryTextColor.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Title
            Text(
                text = "Writing Suggestions",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabChip(
                    label = "For You",
                    isSelected = state.selectedTab == 0,
                    textColor = textColor,
                    secondaryColor = secondaryTextColor,
                    onClick = { viewModel.selectTab(0) }
                )
                TabChip(
                    label = "Recent",
                    isSelected = state.selectedTab == 1,
                    textColor = textColor,
                    secondaryColor = secondaryTextColor,
                    onClick = { viewModel.selectTab(1) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestion cards
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = secondaryTextColor.copy(alpha = 0.5f),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                val suggestions = if (state.selectedTab == 0) {
                    state.forYouSuggestions
                } else {
                    state.recentSuggestions
                }

                if (suggestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Write a few entries to unlock personalized suggestions.",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.sp,
                                color = secondaryTextColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(suggestions, key = { it.id }) { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                cardColor = secondaryTextColor.copy(alpha = 0.05f),
                                onClick = { onSuggestionSelected(it) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TabChip(
    label: String,
    isSelected: Boolean,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) textColor.copy(alpha = 0.08f) else Color.Transparent
    val chipTextColor = if (isSelected) textColor else secondaryColor.copy(alpha = 0.5f)

    Text(
        text = label,
        style = TextStyle(
            fontFamily = FontFamily.Default,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = chipTextColor
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
