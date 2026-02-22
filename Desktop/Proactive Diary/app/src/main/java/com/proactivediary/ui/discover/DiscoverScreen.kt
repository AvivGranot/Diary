package com.proactivediary.ui.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.data.discover.DiscoverContent
import com.proactivediary.domain.model.DiscoverEntry
import com.proactivediary.ui.theme.InstrumentSerif
import kotlinx.coroutines.delay

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onWriteAbout: ((String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val bgColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Header — time-aware greeting
        val hour = java.time.LocalTime.now().hour
        val greeting = when {
            hour in 5..11 -> "Good morning"
            hour in 12..16 -> "Good afternoon"
            hour in 17..21 -> "Good evening"
            else -> "Still up?"
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = greeting,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            text = "Wisdom from uncommon minds",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = secondaryColor.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category filter chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = "Today",
                isSelected = state.selectedCategory == null,
                textColor = textColor,
                secondaryColor = secondaryColor,
                onClick = { viewModel.filterByCategory(null) }
            )
            DiscoverContent.categories.forEach { category ->
                CategoryChip(
                    label = category,
                    isSelected = state.selectedCategory == category,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    onClick = { viewModel.filterByCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards list
        if (state.isLoading) {
            // Skeleton loading
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = textColor.copy(alpha = 0.04f)
                    ) {}
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Welcome back banner for lapsed users (7+ days away)
                if (state.welcomeBack.isVisible) {
                    item(key = "welcome_back") {
                        AnimatedVisibility(
                            visible = state.welcomeBack.isVisible,
                            exit = shrinkVertically()
                        ) {
                            WelcomeBackBanner(
                                entryCount = state.welcomeBack.entryCount,
                                totalWords = state.welcomeBack.totalWords,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                onDismiss = { viewModel.dismissWelcomeBack() }
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = state.entries,
                    key = { _, entry -> entry.id }
                ) { index, entry ->
                    // Staggered entrance animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(entry.id) {
                        delay(index * 60L)
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300)
                        )
                    ) {
                        DiscoverCard(
                            entry = entry,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            onWriteAbout = onWriteAbout
                        )
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) textColor else secondaryColor.copy(alpha = 0.85f)
            )
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(1.5.dp)
                    .background(textColor)
            )
        }
    }
}

@Composable
private fun DiscoverCard(
    entry: DiscoverEntry,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onWriteAbout: ((String) -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            // Decorative opening quote mark
            Text(
                text = "\u201C",
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontSize = 28.sp,
                    color = textColor.copy(alpha = 0.06f)
                ),
                modifier = Modifier.align(Alignment.TopStart)
            )

            Column(modifier = Modifier.padding(top = 12.dp)) {
                // Excerpt
                Text(
                    text = entry.excerpt,
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = textColor.copy(alpha = 0.85f),
                        lineHeight = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Author + years
                Text(
                    text = "${entry.author}  \u00b7  ${entry.authorYears}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryColor
                    )
                )

                // Source + category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.source}  \u00b7  ${entry.category}",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = secondaryColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // "Reflect" pill — obvious, warm, not commanding
                    if (onWriteAbout != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = secondaryColor.copy(alpha = 0.06f),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onWriteAbout("\u201C${entry.excerpt}\u201D \u2014 ${entry.author}")
                            }
                        ) {
                            Text(
                                text = "Reflect",
                                style = TextStyle(
                                    fontFamily = InstrumentSerif,
                                    fontSize = 12.sp,
                                    color = secondaryColor.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeBackBanner(
    entryCount: Int,
    totalWords: Int,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = secondaryColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.padding(end = 24.dp)) {
                Text(
                    text = "Welcome back.",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 22.sp,
                        color = textColor
                    )
                )

                if (entryCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You have $entryCount ${if (entryCount == 1) "entry" else "entries"} and $totalWords words.",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            color = secondaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Day 1 of a new practice.",
                    style = TextStyle(
                        fontFamily = InstrumentSerif,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = textColor.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
