package com.proactivediary.ui.write

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun WriteScreen(
    viewModel: WriteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (!state.isLoaded) return

    val bgColor = DiaryThemeConfig.colorForKey(state.colorKey)
    val textColor = DiaryThemeConfig.textColorFor(state.colorKey)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(state.colorKey)

    val showDateHeader = viewModel.hasFeature("date_header")
    val showAutoSave = viewModel.hasFeature("auto_save")
    val showWordCount = viewModel.hasFeature("word_count")

    val horizontalPadding = when (state.form) {
        "spacious" -> 24.dp
        "compact" -> 16.dp
        else -> 32.dp // "focused"
    }

    val lineHeightMultiplier = when (state.form) {
        "spacious" -> 2.0f
        "compact" -> 1.5f
        else -> 1.7f // "focused"
    }

    var titleExpanded by remember { mutableStateOf(state.title.isNotBlank()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Personalization mark — header position
                if (state.markText.isNotBlank() && state.markPosition == "header") {
                    PersonalizationMark(
                        text = state.markText,
                        font = state.markFont,
                        color = secondaryTextColor.copy(alpha = 0.6f),
                        horizontalPadding = horizontalPadding
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Date header
                if (showDateHeader) {
                    Text(
                        text = state.dateHeader,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 16.sp,
                            color = secondaryTextColor
                        ),
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Title field
                if (titleExpanded) {
                    BasicTextField(
                        value = state.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        textStyle = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor
                        ),
                        cursorBrush = SolidColor(textColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.title.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        style = TextStyle(
                                            fontFamily = CormorantGaramond,
                                            fontSize = 20.sp,
                                            color = secondaryTextColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "tap to add title",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            color = secondaryTextColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                titleExpanded = true
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Writing area with canvas background
                WriteArea(
                    content = state.content,
                    onContentChanged = { viewModel.onContentChanged(it) },
                    canvas = state.canvas,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    horizontalPadding = horizontalPadding,
                    fontSizeSp = state.fontSize,
                    lineHeightMultiplier = lineHeightMultiplier,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Personalization mark — footer position
                if (state.markText.isNotBlank() && state.markPosition == "footer") {
                    Spacer(modifier = Modifier.height(8.dp))
                    PersonalizationMark(
                        text = state.markText,
                        font = state.markFont,
                        color = secondaryTextColor.copy(alpha = 0.6f),
                        horizontalPadding = horizontalPadding
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom toolbar
            WriteToolbar(
                selectedMood = state.mood,
                onMoodSelected = { viewModel.onMoodSelected(it) },
                tags = state.tags,
                onTagsUpdated = { viewModel.onTagsUpdated(it) },
                wordCount = state.wordCount,
                showWordCount = showWordCount,
                colorKey = state.colorKey
            )
        }

        // Auto-save indicator dot (top-right)
        if (showAutoSave) {
            AutoSaveDot(
                isSaving = state.isSaving,
                color = secondaryTextColor.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun PersonalizationMark(
    text: String,
    font: String,
    color: Color,
    horizontalPadding: Dp
) {
    val markFontFamily = when (font) {
        "serif" -> CormorantGaramond
        else -> FontFamily.Default
    }
    Text(
        text = text,
        style = TextStyle(
            fontFamily = markFontFamily,
            fontSize = 14.sp,
            color = color,
            fontStyle = FontStyle.Italic
        ),
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
    )
}

@Composable
private fun WriteArea(
    content: String,
    onContentChanged: (String) -> Unit,
    canvas: String,
    textColor: Color,
    secondaryTextColor: Color,
    horizontalPadding: Dp,
    fontSizeSp: Int,
    lineHeightMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val fontSize = fontSizeSp.sp
    val lineHeightSp = fontSize * lineHeightMultiplier
    val density = LocalDensity.current
    val lineHeightPx = with(density) { lineHeightSp.toPx() }
    val leftMarginDpForNumbered = 36.dp
    val canvasLineColor = secondaryTextColor.copy(alpha = 0.1f)
    val dotColor = secondaryTextColor.copy(alpha = 0.15f)
    val gridColor = secondaryTextColor.copy(alpha = 0.08f)
    val numberColor = secondaryTextColor.copy(alpha = 0.3f)

    Box(modifier = modifier) {
        // Canvas lines drawn behind text
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
        ) {
            val width = size.width
            val height = size.height
            val lineCount = (height / lineHeightPx).toInt() + 1

            when (canvas) {
                "lined" -> {
                    for (i in 1..lineCount) {
                        val y = i * lineHeightPx
                        drawLine(
                            color = canvasLineColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = with(density) { 0.5.dp.toPx() }
                        )
                    }
                }
                "dotted" -> {
                    val horizontalSpacingPx = with(density) { 24.dp.toPx() }
                    val dotRadius = with(density) { 2.dp.toPx() }
                    for (row in 1..lineCount) {
                        val y = row * lineHeightPx
                        var x = 0f
                        while (x <= width) {
                            drawCircle(
                                color = dotColor,
                                radius = dotRadius,
                                center = Offset(x, y)
                            )
                            x += horizontalSpacingPx
                        }
                    }
                }
                "grid" -> {
                    val horizontalSpacingPx = with(density) { 24.dp.toPx() }
                    for (i in 1..lineCount) {
                        val y = i * lineHeightPx
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = with(density) { 0.5.dp.toPx() }
                        )
                    }
                    var x = 0f
                    while (x <= width) {
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = with(density) { 0.5.dp.toPx() }
                        )
                        x += horizontalSpacingPx
                    }
                }
                "numbered" -> {
                    for (i in 1..lineCount) {
                        val y = i * lineHeightPx
                        drawLine(
                            color = canvasLineColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = with(density) { 0.5.dp.toPx() }
                        )
                    }
                }
                // "blank" -> draw nothing
            }
        }

        // Numbered line numbers overlay
        if (canvas == "numbered") {
            val contentLines = if (content.isEmpty()) 1 else content.lines().size
            val totalDisplayLines = maxOf(contentLines, 20)
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                for (i in 1..totalDisplayLines) {
                    Box(
                        modifier = Modifier.height(with(density) { lineHeightSp.toDp() }),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "$i",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 10.sp,
                                color = numberColor
                            )
                        )
                    }
                }
            }
        }

        // Text input
        val leftPadding = if (canvas == "numbered") leftMarginDpForNumbered else horizontalPadding

        BasicTextField(
            value = content,
            onValueChange = onContentChanged,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = fontSize,
                color = textColor,
                lineHeight = lineHeightSp
            ),
            cursorBrush = SolidColor(textColor),
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPadding, end = horizontalPadding),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "Start writing...",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = fontSize,
                                color = secondaryTextColor.copy(alpha = 0.35f),
                                lineHeight = lineHeightSp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun AutoSaveDot(
    isSaving: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isSaving) {
        if (isSaving) {
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 150)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150)
            )
        }
    }

    Box(
        modifier = modifier
            .size(6.dp)
            .scale(scale.value)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}
