package com.proactivediary.ui.designstudio.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.ui.designstudio.components.SectionDivider
import com.proactivediary.ui.designstudio.components.SectionHeader
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

private data class CanvasOption(
    val key: String,
    val name: String,
    val description: String
)

private val canvasOptions = listOf(
    CanvasOption("lined", "Lined", "Traditional guidance for flowing thoughts"),
    CanvasOption("blank", "Blank", "Complete freedom. No boundaries."),
    CanvasOption("dotted", "Dotted", "Subtle guidance without rigidity"),
    CanvasOption("grid", "Grid", "Structure for the organized mind"),
    CanvasOption("numbered", "Numbered", "Every line counts. Literally.")
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CanvasSection(
    selectedCanvas: String,
    onCanvasSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialPage = canvasOptions.indexOfFirst { it.key == selectedCanvas }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { canvasOptions.size }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            onCanvasSelected(canvasOptions[page].key)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            number = "03",
            title = "Canvas",
            subtitle = "The lines on your page shape the thoughts you put on them"
        )

        // Horizontal pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 24.dp)
        ) { page ->
            val option = canvasOptions[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DiaryColors.Parchment),
                contentAlignment = Alignment.Center
            ) {
                CanvasPreview(canvas = option.key)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Style name and description
        val currentOption = canvasOptions.getOrElse(pagerState.currentPage) { canvasOptions[0] }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentOption.name,
                fontFamily = CormorantGaramond,
                fontSize = 18.sp,
                color = DiaryColors.Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = currentOption.description,
                fontFamily = CormorantGaramond,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = DiaryColors.Pencil,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                canvasOptions.forEachIndexed { index, _ ->
                    val isActive = index == pagerState.currentPage
                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.3f,
                        animationSpec = tween(durationMillis = 200),
                        label = "dotAlpha_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) DiaryColors.Ink
                                else DiaryColors.Pencil.copy(alpha = dotAlpha)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionDivider()

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun CanvasPreview(canvas: String) {
    val lineColor = DiaryColors.Pencil.copy(alpha = 0.3f)

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        when (canvas) {
            "lined" -> {
                repeat(8) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(lineColor)
                    )
                }
            }
            "blank" -> {
                // Empty — that's the point
                Spacer(modifier = Modifier.height(64.dp))
            }
            "dotted" -> {
                repeat(6) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(12) {
                            Box(
                                modifier = Modifier
                                    .size(2.dp)
                                    .clip(CircleShape)
                                    .background(lineColor)
                            )
                        }
                    }
                }
            }
            "grid" -> {
                // Horizontal lines
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(lineColor)
                        )
                    }
                }
            }
            "numbered" -> {
                (1..7).forEach { num ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$num",
                            fontSize = 9.sp,
                            color = lineColor,
                            modifier = Modifier.width(16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(lineColor)
                        )
                    }
                }
            }
        }
    }
}
