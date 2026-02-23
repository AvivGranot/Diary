package com.proactivediary.ui.journal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.ChipShape
import com.proactivediary.ui.theme.PillShape
import kotlinx.coroutines.delay

/**
 * Apple Spotlight–style search bar with cycling placeholder and suggestion chips.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val pencilColor = MaterialTheme.colorScheme.onSurfaceVariant
    var isFocused by remember { mutableStateOf(false) }

    // Cycling placeholder texts — rotates every 3 seconds
    val placeholders = remember {
        listOf(
            "Search your diary\u2026",
            "feeling happy\u2026",
            "last week\u2026",
            "#travel\u2026",
            "grateful for\u2026"
        )
    }
    var currentPlaceholderIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholders.size
        }
    }

    // Suggestion chips — shown when focused and query is empty
    val suggestionChips = remember {
        listOf("feeling happy", "last week", "#travel", "grateful", "morning")
    }

    Column(modifier = modifier) {
        // Search bar — 48dp, PillShape, glass background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(PillShape)
                    .background(surfaceColor.copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = pencilColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(pencilColor),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                // Cycling placeholder with crossfade
                                AnimatedContent(
                                    targetState = currentPlaceholderIndex,
                                    transitionSpec = {
                                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                                    },
                                    label = "placeholder_cycle"
                                ) { index ->
                                    Text(
                                        text = placeholders[index],
                                        style = TextStyle(
                                            fontFamily = FontFamily.Default,
                                            fontSize = 15.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = pencilColor.copy(alpha = 0.35f)
                                        )
                                    )
                                }
                            }
                            innerTextField()
                        }
                    }
                )

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = pencilColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Suggestion chips — horizontal scroll, shown when focused with empty query
        if (isFocused && query.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { chip ->
                    Surface(
                        modifier = Modifier.clickable { onQueryChanged(chip) },
                        shape = ChipShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = chip,
                            style = MaterialTheme.typography.labelSmall,
                            color = pencilColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
