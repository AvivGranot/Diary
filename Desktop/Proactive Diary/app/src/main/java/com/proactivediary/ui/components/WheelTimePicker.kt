package com.proactivediary.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * iOS-style wheel time picker with scrollable hour, minute, and AM/PM columns.
 * Each column snaps to items and has a highlighted selection band.
 *
 * @param hour current hour in 24h format (0-23)
 * @param minute current minute (0-59)
 * @param onTimeChanged called with (hour24, minute) when selection changes
 */
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val amPm = if (hour < 12) 0 else 1 // 0 = AM, 1 = PM
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour column (1-12)
        WheelColumn(
            items = (1..12).map { String.format(Locale.US, "%d", it) },
            initialIndex = displayHour - 1,
            onSelectedChanged = { index ->
                val newDisplayHour = index + 1
                val newHour24 = when {
                    amPm == 0 && newDisplayHour == 12 -> 0
                    amPm == 1 && newDisplayHour == 12 -> 12
                    amPm == 1 -> newDisplayHour + 12
                    else -> newDisplayHour
                }
                onTimeChanged(newHour24, minute)
            },
            modifier = Modifier.width(60.dp)
        )

        // Colon separator
        Text(
            text = ":",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        // Minute column (00-59)
        WheelColumn(
            items = (0..59).map { String.format(Locale.US, "%02d", it) },
            initialIndex = minute,
            onSelectedChanged = { newMinute ->
                onTimeChanged(hour, newMinute)
            },
            modifier = Modifier.width(60.dp)
        )

        // AM/PM column
        WheelColumn(
            items = listOf("AM", "PM"),
            initialIndex = amPm,
            onSelectedChanged = { newAmPmIndex ->
                val displayH = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val newHour24 = when {
                    newAmPmIndex == 0 && displayH == 12 -> 0
                    newAmPmIndex == 1 && displayH == 12 -> 12
                    newAmPmIndex == 1 -> displayH + 12
                    else -> displayH
                }
                onTimeChanged(newHour24, minute)
            },
            modifier = Modifier.width(56.dp)
        )
    }
}

/**
 * A single scrollable wheel column with snap-to-item behavior.
 * Shows 3 visible items with the center item highlighted.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    items: List<String>,
    initialIndex: Int,
    onSelectedChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleItems = 3
    val itemHeight = 44.dp

    // Buffer items above and below to center the first/last items
    val bufferSize = visibleItems / 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

            layoutInfo.visibleItemsInfo
                .minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }
                ?.let { it.index - bufferSize }
                ?.coerceIn(0, items.lastIndex)
                ?: initialIndex
        }
    }

    // Emit selection changes
    LaunchedEffect(Unit) {
        snapshotFlow { selectedIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onSelectedChanged(index)
                }
            }
    }

    val highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(modifier = modifier.height(itemHeight * visibleItems)) {
        // Selection highlight band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .background(highlightColor, RoundedCornerShape(8.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.matchParentSize()
        ) {
            // Buffer items (empty) before real items
            items(bufferSize) {
                Box(modifier = Modifier.height(itemHeight))
            }

            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = TextStyle(
                            fontSize = if (isSelected) 22.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.35f)
                    )
                }
            }

            // Buffer items (empty) after real items
            items(bufferSize) {
                Box(modifier = Modifier.height(itemHeight))
            }
        }
    }
}
