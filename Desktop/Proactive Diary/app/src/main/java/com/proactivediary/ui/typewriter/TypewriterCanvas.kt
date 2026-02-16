package com.proactivediary.ui.typewriter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.theme.CormorantGaramond

/**
 * Canvas-based composable that renders the typewriter quote character by character.
 * Uses TextMeasurer for precise character-level positioning on a Compose Canvas.
 *
 * The quote is split by explicit newlines. Each line is rendered independently so that
 * character reveals happen one character at a time across line boundaries. The cursor
 * always tracks the position immediately after the last visible character.
 */
@Composable
fun TypewriterCanvas(
    quote: String,
    visibleCharCount: Int,
    cursorVisible: Boolean,
    cursorAlpha: Float,
    cursorBlinkOn: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val textColor = Color.White.copy(alpha = 0.85f)
    val cursorColor = Color.White

    val textStyle = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 18.sp,
        fontStyle = FontStyle.Italic,
        lineHeight = 28.sp,
        color = textColor
    )

    // Pre-split quote into lines for manual line-by-line layout
    val lines = remember(quote) { quote.split("\n") }

    val lineHeightPx = with(density) { 30.sp.toPx() }
    val cursorWidthPx = with(density) { 1.dp.toPx() }
    val cursorPaddingPx = with(density) { 1.dp.toPx() }

    // Total canvas height: one lineHeight per line, plus a small buffer
    val canvasHeightDp: Dp = with(density) {
        (lineHeightPx * lines.size).toDp() + 4.dp
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(canvasHeightDp)
    ) {
        var cursorX = 0f
        var cursorY = 0f

        for ((lineIndex, line) in lines.withIndex()) {
            val baselineY = lineIndex * lineHeightPx

            // Number of characters in the original quote string before this line starts.
            // Each preceding line contributes its length plus 1 for the '\n' separator.
            val charsBeforeLine = lines.take(lineIndex).sumOf { it.length } + lineIndex

            // How many characters of this particular line should be visible
            val charsToShow = (visibleCharCount - charsBeforeLine).coerceIn(0, line.length)

            // Measure the full line to calculate center offset
            val fullLineResult = textMeasurer.measure(
                text = line,
                style = textStyle
            )
            val lineWidth = fullLineResult.size.width.toFloat()
            val centerOffsetX = (size.width - lineWidth) / 2f

            if (charsToShow > 0) {
                val visibleText = line.substring(0, charsToShow)
                val textResult = textMeasurer.measure(
                    text = visibleText,
                    style = textStyle
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(centerOffsetX, baselineY)
                )

                // Place cursor right after the last drawn character on this line
                cursorX = centerOffsetX + textResult.size.width.toFloat() + cursorPaddingPx
                cursorY = baselineY
            } else if (charsBeforeLine <= visibleCharCount && lineIndex > 0) {
                // The newline before this line has been "typed" but no chars on this line yet.
                // Cursor should sit at the beginning of this line (centered).
                cursorX = centerOffsetX + cursorPaddingPx
                cursorY = baselineY
            }
        }

        // When no characters are visible yet, place cursor at origin
        if (visibleCharCount == 0) {
            cursorX = 0f
            cursorY = 0f
        }

        // Draw the cursor as a thin vertical rectangle
        if (cursorVisible && cursorBlinkOn && cursorAlpha > 0f) {
            drawRect(
                color = cursorColor.copy(alpha = cursorAlpha),
                topLeft = Offset(cursorX, cursorY),
                size = Size(cursorWidthPx, lineHeightPx)
            )
        }
    }
}
