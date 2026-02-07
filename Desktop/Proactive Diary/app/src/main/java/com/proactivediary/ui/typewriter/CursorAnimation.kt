package com.proactivediary.ui.typewriter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * Produces a boolean state that toggles on/off with a step function (no easing)
 * at the specified interval. Used for the typewriter blinking cursor.
 *
 * @param intervalMs The blink interval in milliseconds (full on + off cycle = 2x interval).
 *                   The cursor is ON for [intervalMs] and OFF for [intervalMs].
 * @param enabled Whether the blink animation should run. When false, returns true (cursor on).
 */
@Composable
fun rememberCursorBlinkState(
    intervalMs: Long = 530L,
    enabled: Boolean = true
): State<Boolean> {
    val blinkState = remember { mutableStateOf(true) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            blinkState.value = true
            return@LaunchedEffect
        }
        // Step-function blink: hard toggle, no interpolation
        while (true) {
            blinkState.value = true
            delay(intervalMs)
            blinkState.value = false
            delay(intervalMs)
        }
    }

    return blinkState
}
