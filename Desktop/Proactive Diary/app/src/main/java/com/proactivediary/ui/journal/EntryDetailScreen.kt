package com.proactivediary.ui.journal

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.share.ShareCardData
import com.proactivediary.ui.share.ShareCardDialog
import com.proactivediary.ui.share.shareCardAsImage
import androidx.compose.foundation.layout.navigationBarsPadding
import com.proactivediary.ui.theme.CormorantGaramond

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    canEdit: Boolean = true,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasNavigatedBack by remember { mutableStateOf(false) }
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted && !hasNavigatedBack) {
            hasNavigatedBack = true
            onBack()
        }
    }

    if (!state.isLoaded) return

    val bgColor = DiaryThemeConfig.colorForKey(state.colorKey)
    val textColor = DiaryThemeConfig.textColorFor(state.colorKey)
    val secondaryTextColor = DiaryThemeConfig.secondaryTextColorFor(state.colorKey)

    val horizontalPadding = when (state.form) {
        "spacious" -> 24.dp
        "compact" -> 16.dp
        else -> 32.dp
    }

    val lineHeightMultiplier = when (state.form) {
        "spacious" -> 2.0f
        "compact" -> 1.5f
        else -> 1.7f
    }

    val fontSize = state.fontSize.sp
    val lineHeightSp = fontSize * lineHeightMultiplier

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .navigationBarsPadding()
    ) {
        // Top bar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
            },
            actions = {
                if (canEdit) {
                    IconButton(onClick = { onEdit(state.entryId) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = textColor
                        )
                    }
                }
                IconButton(onClick = { showShareDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = textColor
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More",
                            tint = textColor
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share as image") },
                            onClick = {
                                showMenu = false
                                showShareDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Mark — header
            if (state.markText.isNotBlank() && state.markPosition == "header") {
                val markFont = if (state.markFont == "serif") CormorantGaramond else FontFamily.Default
                Text(
                    text = state.markText,
                    style = TextStyle(
                        fontFamily = markFont,
                        fontSize = 14.sp,
                        color = secondaryTextColor.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Date header
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

            // Title
            if (state.title.isNotBlank()) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = textColor
                    ),
                    modifier = Modifier.padding(horizontal = horizontalPadding)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content with canvas background
            val density = LocalDensity.current
            val lineHeightPx = with(density) { lineHeightSp.toPx() }
            val contentLines = if (state.content.isEmpty()) 1 else state.content.lines().size
            val estimatedHeight = (contentLines * lineHeightPx / density.density).dp + 48.dp
            val canvasLineColor = secondaryTextColor.copy(alpha = 0.1f)
            val dotColor = secondaryTextColor.copy(alpha = 0.15f)
            val gridColor = secondaryTextColor.copy(alpha = 0.08f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(estimatedHeight)
            ) {
                // Canvas lines behind text
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding)
                ) {
                    val width = size.width
                    val height = size.height
                    val lineCount = (height / lineHeightPx).toInt() + 1

                    when (state.canvas) {
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
                            val hSpacing = with(density) { 24.dp.toPx() }
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
                                    x += hSpacing
                                }
                            }
                        }
                        "grid" -> {
                            val hSpacing = with(density) { 24.dp.toPx() }
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
                                x += hSpacing
                            }
                        }
                    }
                }

                // Content text
                Text(
                    text = state.content,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = fontSize,
                        color = textColor,
                        lineHeight = lineHeightSp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mood + tags + word count at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.mood != null) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(state.mood!!.color)
                    )
                }

                if (state.tags.isNotEmpty()) {
                    Text(
                        text = state.tags.joinToString(" ") { "#$it" },
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${state.wordCount} words",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = secondaryTextColor
                    )
                )
            }

            // Mark — footer
            if (state.markText.isNotBlank() && state.markPosition == "footer") {
                Spacer(modifier = Modifier.height(16.dp))
                val markFont = if (state.markFont == "serif") CormorantGaramond else FontFamily.Default
                Text(
                    text = state.markText,
                    style = TextStyle(
                        fontFamily = markFont,
                        fontSize = 14.sp,
                        color = secondaryTextColor.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete entry") },
            text = { Text("Delete this entry? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEntry()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Share card dialog
    if (showShareDialog) {
        val excerpt = if (state.content.length > 300) {
            state.content.take(300).trimEnd() + "..."
        } else {
            state.content
        }

        ShareCardDialog(
            data = ShareCardData(
                excerpt = excerpt,
                title = state.title,
                dateFormatted = state.dateHeader,
                colorKey = state.colorKey,
                mood = state.mood?.key
            ),
            onDismiss = { showShareDialog = false },
            onShare = { bitmap ->
                shareCardAsImage(context, bitmap)
                showShareDialog = false
            }
        )
    }
}
