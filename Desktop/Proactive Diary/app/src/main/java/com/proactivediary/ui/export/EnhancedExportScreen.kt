package com.proactivediary.ui.export

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.components.HeroSection
import com.proactivediary.ui.components.PrivacyBadge
import com.proactivediary.ui.theme.DiarySpacing
import com.proactivediary.ui.theme.InstrumentSerif
import com.proactivediary.ui.theme.LocalDiaryExtendedColors
import com.proactivediary.ui.theme.PillShape
import com.proactivediary.ui.theme.PlusJakartaSans
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedExportScreen(
    onBack: () -> Unit,
    viewModel: EnhancedExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val extendedColors = LocalDiaryExtendedColors.current

    var selectedFormat by remember { mutableStateOf(ExportFormat.ZIP) }
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    // Share the exported file once ready
    LaunchedEffect(uiState.exportedFile) {
        uiState.exportedFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val mimeType = when (selectedFormat) {
                    ExportFormat.JSON -> "application/json"
                    ExportFormat.PDF -> "application/pdf"
                    ExportFormat.ZIP -> "application/zip"
                    ExportFormat.MARKDOWN -> "text/markdown"
                    ExportFormat.PLAIN_TEXT -> "text/plain"
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share export"))
            } catch (_: Exception) { }
            viewModel.clearExportedFile()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Back button ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DiarySpacing.xs, vertical = DiarySpacing.xxs)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = 180f)
                    )
                }
            }

            // ── Hero section ──────────────────────────────────────
            HeroSection(
                title = "Your journal, your way.",
                subtitle = "Take your memories anywhere."
            )

            Column(
                modifier = Modifier.padding(horizontal = DiarySpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(DiarySpacing.sm)
            ) {
                Text(
                    text = "Pick a format",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── Format options ──────────────────────────────
                FormatOption(
                    icon = Icons.Outlined.Archive,
                    title = "ZIP (with media)",
                    description = "Entries JSON + all images and audio files",
                    selected = selectedFormat == ExportFormat.ZIP,
                    onClick = { selectedFormat = ExportFormat.ZIP }
                )

                FormatOption(
                    icon = Icons.Outlined.Code,
                    title = "JSON",
                    description = "Structured entry data, easy to import",
                    selected = selectedFormat == ExportFormat.JSON,
                    onClick = { selectedFormat = ExportFormat.JSON }
                )

                FormatOption(
                    icon = Icons.Outlined.PictureAsPdf,
                    title = "PDF",
                    description = "Formatted document with styling",
                    selected = selectedFormat == ExportFormat.PDF,
                    onClick = { selectedFormat = ExportFormat.PDF }
                )

                FormatOption(
                    icon = Icons.Outlined.Description,
                    title = "Markdown",
                    description = "Plain text with formatting, works everywhere",
                    selected = selectedFormat == ExportFormat.MARKDOWN,
                    onClick = { selectedFormat = ExportFormat.MARKDOWN }
                )

                FormatOption(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    title = "Plain Text",
                    description = "Simple text file, universal compatibility",
                    selected = selectedFormat == ExportFormat.PLAIN_TEXT,
                    onClick = { selectedFormat = ExportFormat.PLAIN_TEXT }
                )

                Spacer(Modifier.height(8.dp))

                // ── Date range (optional) ───────────────────────
                Text(
                    text = "Date range (optional)",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showDatePicker = true }
                        .padding(DiarySpacing.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val rangeText = if (startDate != null && endDate != null) {
                        "${dateFormat.format(Date(startDate!!))} - ${dateFormat.format(Date(endDate!!))}"
                    } else {
                        "All entries"
                    }
                    Text(
                        text = rangeText,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            color = if (startDate != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (startDate != null) {
                        TextButton(onClick = {
                            startDate = null
                            endDate = null
                        }) {
                            Text(
                                "Clear",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Progress indicator ──────────────────────────
                if (uiState.isExporting) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = uiState.progress,
                        label = "export_progress"
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = extendedColors.accent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Exporting... ${(animatedProgress * 100).toInt()}%",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Export button ────────────────────────────────
                Button(
                    onClick = {
                        viewModel.export(
                            format = selectedFormat,
                            startDate = startDate,
                            endDate = endDate
                        )
                    },
                    enabled = !uiState.isExporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.background,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Prepare export",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.background
                        )
                    }
                }

                Spacer(Modifier.height(DiarySpacing.md))

                // Trust badge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PrivacyBadge(text = "Data never leaves your device during export")
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // ── Date range picker dialog ────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedStartDateMillis
                    endDate = datePickerState.selectedEndDateMillis
                    showDatePicker = false
                }) {
                    Text("Confirm", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.height(460.dp),
                title = {
                    Text(
                        text = "Select date range",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun FormatOption(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = LocalDiaryExtendedColors.current
    val borderColor = if (selected) extendedColors.accent
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) extendedColors.accent.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(DiarySpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = if (selected) extendedColors.accent
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(DiarySpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = extendedColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class ExportFormat {
    JSON, PDF, ZIP, MARKDOWN, PLAIN_TEXT
}
