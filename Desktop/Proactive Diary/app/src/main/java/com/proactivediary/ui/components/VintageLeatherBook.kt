package com.proactivediary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

// ── Color palette ──
private val Leather = Color(0xFF6B3A2A)
private val LeatherShadow = Color(0xFF4A2518)
private val LeatherHighlight = Color(0xFF8B5E3C)
private val GoldAged = Color(0xFFC9A84C)
private val GoldLight = Color(0xFFE8D08C)
private val GoldDark = Color(0xFF967530)
private val PageCream = Color(0xFFF5EDD6)
private val PageAged = Color(0xFFEDE0C8)
private val PageEdgeShadow = Color(0xFFD4C5A9)
private val SpineDark = Color(0xFF3D2215)
private val SpineEdge = Color(0xFF5A3525)

/**
 * Cinematic vintage leather book that opens to the middle.
 *
 * @param openProgress 0f = closed, 1f = fully open
 * @param showWritingLines true draws progressive text lines (Fallback screen)
 */
@Composable
fun VintageLeatherBook(
    openProgress: Float,
    modifier: Modifier = Modifier,
    showWritingLines: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val bookT = h * 0.04f
        val bookB = h * 0.96f
        val bookH = bookB - bookT
        val halfW = w * 0.44f
        val spineW = w * 0.07f

        // ── 1. Soft drop shadow ──
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(cx - halfW - 4f, bookT + 4f),
            size = Size(halfW * 2 + 8f, bookH + 4f),
            cornerRadius = CornerRadius(10f)
        )

        // ── 2. Page fan (6 individual pages) ──
        val pageAlpha = (openProgress * 1.8f).coerceIn(0f, 1f)
        if (openProgress > 0.15f) {
            for (i in 0 until 6) {
                val pageStart = 0.30f + i * 0.04f
                val pageProg = ((openProgress - pageStart) / 0.35f).coerceIn(0f, 1f)
                val fanAngle = pageProg * (8f + i * 3f)
                val shade = PageCream.copy(alpha = pageAlpha * (0.4f + i * 0.1f))
                val edgeShade = GoldAged.copy(alpha = pageAlpha * 0.15f)

                // Left fan page
                rotate(-fanAngle, Offset(cx, cy)) {
                    drawRoundRect(shade, Offset(cx - halfW + 8f, bookT + 5f), Size(halfW - 12f, bookH - 10f), CornerRadius(2f))
                    // Gold gilded bottom edge
                    drawLine(edgeShade, Offset(cx - halfW + 10f, bookB - 6f), Offset(cx - 4f, bookB - 6f), 1f)
                }
                // Right fan page
                rotate(fanAngle, Offset(cx, cy)) {
                    drawRoundRect(shade, Offset(cx + 4f, bookT + 5f), Size(halfW - 12f, bookH - 10f), CornerRadius(2f))
                    drawLine(edgeShade, Offset(cx + 4f, bookB - 6f), Offset(cx + halfW - 10f, bookB - 6f), 1f)
                }
            }
        }

        // ── 3. Left/Right page blocks ──
        val blockAlpha = (openProgress * 1.5f).coerceIn(0f, 1f)
        val leftL = cx - halfW + 6f
        val leftR = cx - spineW / 2f
        val rightL = cx + spineW / 2f
        val rightR = cx + halfW - 6f

        // Left page surface
        drawRect(PageCream.copy(alpha = blockAlpha * 0.92f), Offset(leftL, bookT + 4f), Size(leftR - leftL, bookH - 8f))
        // Aged edge lines at bottom
        val pageThick = bookH * 0.055f
        for (i in 0 until 16) {
            val y = bookB - pageThick + pageThick * (i / 16f)
            val edgeA = blockAlpha * (0.2f + i * 0.03f)
            drawLine(PageEdgeShadow.copy(alpha = edgeA), Offset(leftL, y), Offset(leftR, y), 0.6f)
        }

        // Right page surface
        drawRect(PageCream.copy(alpha = blockAlpha * 0.92f), Offset(rightL, bookT + 4f), Size(rightR - rightL, bookH - 8f))
        for (i in 0 until 16) {
            val y = bookB - pageThick + pageThick * (i / 16f)
            val edgeA = blockAlpha * (0.2f + i * 0.03f)
            drawLine(PageEdgeShadow.copy(alpha = edgeA), Offset(rightL, y), Offset(rightR, y), 0.6f)
        }

        // ── 4. Writing lines (conditional) ──
        if (showWritingLines && openProgress > 0.4f) {
            val lineAlpha = ((openProgress - 0.4f) / 0.4f).coerceIn(0f, 1f)
            for (i in 0..6) {
                val y = bookT + bookH * (0.1f + i * 0.1f)
                val lineProg = ((lineAlpha - i * 0.06f) / 0.4f).coerceIn(0f, 1f)
                val a = 0.15f * lineAlpha
                // Left: spine outward
                drawLine(LeatherShadow.copy(alpha = a), Offset(leftR - 2f, y), Offset(leftR - 2f - (leftR - leftL - 12f) * lineProg, y), 0.7f)
                // Right: spine outward
                drawLine(LeatherShadow.copy(alpha = a * 0.85f), Offset(rightL + 2f, y), Offset(rightL + 2f + (rightR - rightL - 12f) * lineProg, y), 0.7f)
            }
            // Thicker writing marks
            if (openProgress > 0.6f) {
                val wa = ((openProgress - 0.6f) / 0.4f).coerceIn(0f, 1f) * 0.12f
                for (i in 0..3) {
                    val y = bookT + bookH * (0.1f + i * 0.1f)
                    val markLen = (leftR - leftL - 12f) * (0.35f + i * 0.12f)
                    drawLine(LeatherShadow.copy(alpha = wa), Offset(leftR - 2f, y - 2f), Offset(leftR - 2f - markLen, y - 2f), 1.5f)
                }
                for (i in 0..2) {
                    val y = bookT + bookH * (0.1f + i * 0.1f)
                    val markLen = (rightR - rightL - 12f) * (0.3f + i * 0.1f)
                    drawLine(LeatherShadow.copy(alpha = wa * 0.8f), Offset(rightL + 2f, y - 2f), Offset(rightL + 2f + markLen, y - 2f), 1.5f)
                }
            }
        }

        // ── 5. Left cover ──
        rotate(openProgress * 170f, Offset(cx, cy)) {
            val coverLeft = cx - halfW
            // Base leather
            drawRoundRect(Leather, Offset(coverLeft, bookT), Size(halfW, bookH), CornerRadius(8f))
            // Radial gradient overlay — worn leather texture
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(LeatherHighlight.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(coverLeft + halfW * 0.45f, bookT + bookH * 0.4f),
                    radius = halfW * 0.7f
                ),
                topLeft = Offset(coverLeft, bookT),
                size = Size(halfW, bookH)
            )
            // 3D depth shadow (bottom/right edges)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, LeatherShadow.copy(alpha = 0.4f)),
                    start = Offset(coverLeft, bookT),
                    end = Offset(coverLeft, bookB)
                ),
                topLeft = Offset(coverLeft, bookB - bookH * 0.25f),
                size = Size(halfW, bookH * 0.25f)
            )
            // Outer border
            drawRoundRect(LeatherShadow, Offset(coverLeft, bookT), Size(halfW, bookH), CornerRadius(8f), style = Stroke(1.8f))

            // Double gold frame
            val frameInset = halfW * 0.1f
            val frameT = bookT + frameInset
            val frameH = bookH - frameInset * 2
            val frameW = halfW - frameInset * 2
            drawRoundRect(GoldAged.copy(alpha = 0.35f), Offset(coverLeft + frameInset, frameT), Size(frameW, frameH), CornerRadius(3f), style = Stroke(0.8f))
            val innerInset = frameInset + halfW * 0.03f
            drawRoundRect(GoldAged.copy(alpha = 0.22f), Offset(coverLeft + innerInset, bookT + innerInset), Size(halfW - innerInset * 2, bookH - innerInset * 2), CornerRadius(2f), style = Stroke(0.5f))

            // Corner filigrees
            val fSize = halfW * 0.08f
            for ((fx, fy, sx, sy) in listOf(
                floatArrayOf(coverLeft + frameInset, frameT, 1f, 1f),
                floatArrayOf(coverLeft + frameInset + frameW, frameT, -1f, 1f),
                floatArrayOf(coverLeft + frameInset, frameT + frameH, 1f, -1f),
                floatArrayOf(coverLeft + frameInset + frameW, frameT + frameH, -1f, -1f)
            )) {
                val curlPath = Path().apply {
                    moveTo(fx, fy)
                    cubicTo(fx + fSize * 0.6f * sx, fy, fx + fSize * sx, fy + fSize * 0.4f * sy, fx + fSize * sx, fy + fSize * sy)
                }
                drawPath(curlPath, GoldAged.copy(alpha = 0.4f), style = Stroke(0.7f))
            }

            // Title plate
            val plateW = halfW * 0.45f
            val plateH = bookH * 0.06f
            val plateX = coverLeft + (halfW - plateW) / 2f
            val plateY = bookT + bookH * 0.42f
            drawRoundRect(GoldAged.copy(alpha = 0.18f), Offset(plateX, plateY), Size(plateW, plateH), CornerRadius(2f))
            drawRoundRect(GoldDark.copy(alpha = 0.35f), Offset(plateX, plateY), Size(plateW, plateH), CornerRadius(2f), style = Stroke(0.7f))
        }

        // ── 6. Right cover (mirrored) ──
        rotate(-openProgress * 170f, Offset(cx, cy)) {
            val coverLeft = cx
            // Base leather
            drawRoundRect(Leather, Offset(coverLeft, bookT), Size(halfW, bookH), CornerRadius(8f))
            // Worn texture
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(LeatherHighlight.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(coverLeft + halfW * 0.55f, bookT + bookH * 0.4f),
                    radius = halfW * 0.7f
                ),
                topLeft = Offset(coverLeft, bookT),
                size = Size(halfW, bookH)
            )
            // 3D depth
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, LeatherShadow.copy(alpha = 0.4f)),
                    start = Offset(coverLeft, bookT),
                    end = Offset(coverLeft, bookB)
                ),
                topLeft = Offset(coverLeft, bookB - bookH * 0.25f),
                size = Size(halfW, bookH * 0.25f)
            )
            // Outer border
            drawRoundRect(LeatherShadow, Offset(coverLeft, bookT), Size(halfW, bookH), CornerRadius(8f), style = Stroke(1.8f))

            // Double gold frame
            val frameInset = halfW * 0.1f
            val frameT = bookT + frameInset
            val frameH = bookH - frameInset * 2
            val frameW = halfW - frameInset * 2
            drawRoundRect(GoldAged.copy(alpha = 0.35f), Offset(coverLeft + frameInset, frameT), Size(frameW, frameH), CornerRadius(3f), style = Stroke(0.8f))
            val innerInset = frameInset + halfW * 0.03f
            drawRoundRect(GoldAged.copy(alpha = 0.22f), Offset(coverLeft + innerInset, bookT + innerInset), Size(halfW - innerInset * 2, bookH - innerInset * 2), CornerRadius(2f), style = Stroke(0.5f))

            // Corner filigrees
            val fSize = halfW * 0.08f
            for ((fx, fy, sx, sy) in listOf(
                floatArrayOf(coverLeft + frameInset, frameT, 1f, 1f),
                floatArrayOf(coverLeft + frameInset + frameW, frameT, -1f, 1f),
                floatArrayOf(coverLeft + frameInset, frameT + frameH, 1f, -1f),
                floatArrayOf(coverLeft + frameInset + frameW, frameT + frameH, -1f, -1f)
            )) {
                val curlPath = Path().apply {
                    moveTo(fx, fy)
                    cubicTo(fx + fSize * 0.6f * sx, fy, fx + fSize * sx, fy + fSize * 0.4f * sy, fx + fSize * sx, fy + fSize * sy)
                }
                drawPath(curlPath, GoldAged.copy(alpha = 0.4f), style = Stroke(0.7f))
            }

            // Title plate
            val plateW = halfW * 0.45f
            val plateH = bookH * 0.06f
            val plateX = coverLeft + (halfW - plateW) / 2f
            val plateY = bookT + bookH * 0.42f
            drawRoundRect(GoldAged.copy(alpha = 0.18f), Offset(plateX, plateY), Size(plateW, plateH), CornerRadius(2f))
            drawRoundRect(GoldDark.copy(alpha = 0.35f), Offset(plateX, plateY), Size(plateW, plateH), CornerRadius(2f), style = Stroke(0.7f))
        }

        // ── 7. Spine ──
        val spineLeft = cx - spineW / 2f
        // Base
        drawRoundRect(SpineDark, Offset(spineLeft, bookT), Size(spineW, bookH), CornerRadius(4f))
        // Edge highlights
        drawLine(SpineEdge.copy(alpha = 0.5f), Offset(spineLeft + 1f, bookT + 4f), Offset(spineLeft + 1f, bookB - 4f), 1f)
        drawLine(SpineEdge.copy(alpha = 0.5f), Offset(spineLeft + spineW - 1f, bookT + 4f), Offset(spineLeft + spineW - 1f, bookB - 4f), 1f)
        // Raised bands (4 ridges)
        for (i in 1..4) {
            val bandY = bookT + bookH * i / 5f
            drawLine(LeatherHighlight.copy(alpha = 0.45f), Offset(spineLeft + 2f, bandY - 1f), Offset(spineLeft + spineW - 2f, bandY - 1f), 1.2f)
            drawLine(SpineDark.copy(alpha = 0.7f), Offset(spineLeft + 2f, bandY + 1f), Offset(spineLeft + spineW - 2f, bandY + 1f), 0.8f)
        }
        // Shadow gradient onto pages from spine
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(SpineDark.copy(alpha = 0.2f), Color.Transparent),
                startX = spineLeft + spineW,
                endX = spineLeft + spineW + halfW * 0.08f
            ),
            topLeft = Offset(spineLeft + spineW, bookT + 4f),
            size = Size(halfW * 0.08f, bookH - 8f)
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, SpineDark.copy(alpha = 0.2f)),
                startX = spineLeft - halfW * 0.08f,
                endX = spineLeft
            ),
            topLeft = Offset(spineLeft - halfW * 0.08f, bookT + 4f),
            size = Size(halfW * 0.08f, bookH - 8f)
        )

        // ── 8. Golden glow (progress > 0.8) ──
        if (openProgress > 0.8f) {
            val glowAlpha = ((openProgress - 0.8f) / 0.2f).coerceIn(0f, 1f) * 0.12f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldLight.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = halfW * 0.6f
                ),
                radius = halfW * 0.6f,
                center = Offset(cx, cy)
            )
        }
    }
}
