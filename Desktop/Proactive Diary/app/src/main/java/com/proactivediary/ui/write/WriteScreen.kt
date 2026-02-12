package com.proactivediary.ui.write

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.components.ImageViewer
import com.proactivediary.ui.share.ShareCardDialog
import com.proactivediary.ui.share.ShareCardData
import com.proactivediary.ui.share.StreakShareData
import com.proactivediary.ui.share.StreakCardPreview
import com.proactivediary.ui.share.shareCardAsImage
import com.proactivediary.ui.suggestions.SuggestionsBottomSheet
import com.proactivediary.ui.theme.CormorantGaramond
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun WriteScreen(
    viewModel: WriteViewModel = hiltViewModel(),
    onOpenDesignStudio: (() -> Unit)? = null,
    onShareStreak: ((Int) -> Unit)? = null,
    onEntrySaved: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var showTagDialog by remember { mutableStateOf(false) }

    // Contact picker launcher — no READ_CONTACTS permission needed
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let { uri ->
            resolveContact(context, uri)?.let { contact ->
                viewModel.onContactTagged(contact)
                // Auto-open share sheet after tagging
                shareEntryWithContact(context, viewModel.uiState.value, contact)
                viewModel.logContactShared(contact.email != null, contact.phone != null)
            }
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addImage(it) }
    }

    // Camera capture launcher
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.addImage(cameraImageUri!!)
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val photoFile = java.io.File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
        }
    }

    var showImagePicker by remember { mutableStateOf(false) }
    var viewingImageId by remember { mutableStateOf<String?>(null) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Refresh entry state when screen resumes (e.g., after deleting from journal detail)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshEntry()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Notify parent when a new entry is saved (for billing refresh)
    LaunchedEffect(state.newEntrySaved) {
        if (state.newEntrySaved) {
            onEntrySaved?.invoke()
            viewModel.clearNewEntrySaved()
        }
    }

    // Trigger in-app review after streak celebration dismiss
    LaunchedEffect(state.requestInAppReview) {
        if (state.requestInAppReview) {
            if (activity != null) {
                viewModel.requestInAppReview(activity)
            } else {
                viewModel.clearInAppReviewRequest()
            }
        }
    }

    if (!state.isLoaded) return

    // Rich text state
    val richTextState = rememberRichTextState()
    var richTextInitialized by remember { mutableStateOf(false) }

    // Initialize rich text state from loaded entry
    LaunchedEffect(state.isLoaded, state.entryId) {
        if (state.isLoaded && !richTextInitialized) {
            if (state.contentHtml != null) {
                richTextState.setHtml(state.contentHtml!!)
            } else if (state.content.isNotEmpty()) {
                richTextState.setText(state.content)
            }
            richTextInitialized = true
        }
    }

    // Propagate rich text changes to ViewModel
    LaunchedEffect(richTextInitialized) {
        if (!richTextInitialized) return@LaunchedEffect
        snapshotFlow { richTextState.annotatedString }
            .collectLatest {
                val html = richTextState.toHtml()
                val plain = richTextState.annotatedString.text
                viewModel.onRichContentChanged(html, plain)
            }
    }

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
            .navigationBarsPadding()
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

                    // Goal progress indicator below date
                    if (state.weeklyGoalProgress != null) {
                        Text(
                            text = state.weeklyGoalProgress!!,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = secondaryTextColor.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Location and weather chips
                    val locName = state.locationName
                    val wTemp = state.weatherTemp
                    val wCond = state.weatherCondition
                    val wIcon = state.weatherIcon
                    if (locName != null || wCond != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (locName != null) {
                                LocationChip(
                                    locationName = locName,
                                    latitude = state.latitude,
                                    longitude = state.longitude,
                                    textColor = secondaryTextColor
                                )
                            }
                            if (wTemp != null && wCond != null && wIcon != null) {
                                WeatherChip(
                                    temperature = wTemp,
                                    condition = wCond,
                                    weatherIcon = wIcon,
                                    textColor = secondaryTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tag row: # text tags + @ contact tags
                ContactTagRow(
                    taggedContacts = state.taggedContacts,
                    textTags = state.tags,
                    onAddContactClick = { contactPickerLauncher.launch(null) },
                    onRemoveContact = { viewModel.onContactRemoved(it) },
                    onShareWithContact = { contact ->
                        shareEntryWithContact(context, state, contact)
                        viewModel.logContactShared(contact.email != null, contact.phone != null)
                    },
                    onTextTagsClick = { showTagDialog = true },
                    secondaryTextColor = secondaryTextColor,
                    horizontalPadding = horizontalPadding
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Image attachment bar
                ImageAttachmentBar(
                    images = state.images,
                    thumbnailProvider = { filename ->
                        viewModel.imageStorageManager.getThumbnailFile(state.entryId, filename)
                    },
                    onAddClick = { showImagePicker = true },
                    onRemoveClick = { imageId -> viewModel.removeImage(imageId) },
                    onImageClick = { imageId -> viewingImageId = imageId },
                    secondaryTextColor = secondaryTextColor
                )

                // Voice recording button
                Box(
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp)
                ) {
                    VoiceRecordButton(
                        isRecording = state.isRecording,
                        onStartRecording = {
                            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                            if (hasPermission) {
                                viewModel.startRecording()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopRecording = { viewModel.stopRecording() },
                        secondaryTextColor = secondaryTextColor
                    )
                }

                // Template prompt banner (when a template is active)
                if (state.templatePrompts.isNotEmpty() && state.currentPromptIndex < state.templatePrompts.size) {
                    GuidedPromptBanner(
                        prompt = state.templatePrompts[state.currentPromptIndex],
                        currentIndex = state.currentPromptIndex,
                        totalPrompts = state.templatePrompts.size,
                        onNext = { viewModel.advancePrompt() },
                        onDismiss = { viewModel.dismissTemplate() },
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp)
                    )
                }

                // Template picker button (only show when no template is active and entry is new)
                if (state.templatePrompts.isEmpty() && state.isNewEntry && state.content.isEmpty()) {
                    Text(
                        text = "Use a template...",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = secondaryTextColor.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding, vertical = 2.dp)
                            .clickable { showTemplatePicker = true }
                    )
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
                    richTextState = richTextState,
                    canvas = state.canvas,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    horizontalPadding = horizontalPadding,
                    fontSizeSp = state.fontSize,
                    lineHeightMultiplier = lineHeightMultiplier,
                    placeholderText = state.dailyPrompt,
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
                wordCount = state.wordCount,
                showWordCount = showWordCount,
                colorKey = state.colorKey,
                richTextState = richTextState,
                onSuggestionsClick = { showSuggestions = true }
            )
        }

        // Auto-save "Saved" indicator (top-right)
        if (showAutoSave) {
            SavedIndicator(
                isSaving = state.isSaving,
                color = secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 16.dp)
            )
        }

        // Goal completion message (top-left, below header area)
        if (state.goalCompletedMessage != null) {
            LaunchedEffect(state.goalCompletedMessage) {
                delay(3000)
                viewModel.dismissGoalCompleted()
            }

            Text(
                text = state.goalCompletedMessage!!,
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF5B8C5A).copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = horizontalPadding, top = 20.dp)
            )
        }
    }

    // Design Studio prompt after first entry save
    if (state.showDesignStudioPrompt && onOpenDesignStudio != null) {
        DesignStudioPrompt(
            onOpenDesignStudio = {
                viewModel.dismissDesignStudioPrompt()
                onOpenDesignStudio()
            },
            onDismiss = { viewModel.dismissDesignStudioPrompt() }
        )
    }

    // Text tag dialog (moved from toolbar)
    if (showTagDialog) {
        TagInputDialog(
            currentTags = state.tags,
            onDismiss = { showTagDialog = false },
            onConfirm = { newTags ->
                viewModel.onTagsUpdated(newTags)
                showTagDialog = false
            }
        )
    }

    // Streak share dialog state
    var streakShareCount by remember { mutableStateOf(0) }

    // Streak milestone celebration overlay
    if (state.showStreakCelebration > 0) {
        StreakCelebration(
            streakCount = state.showStreakCelebration,
            onDismiss = { viewModel.dismissStreakCelebration() },
            onShare = { count ->
                streakShareCount = count
                viewModel.dismissStreakCelebration()
            }
        )
    }

    // First entry "Day 1" celebration
    if (state.showFirstEntryCelebration) {
        FirstEntryCelebration(
            onDismiss = { viewModel.dismissFirstEntryCelebration() }
        )
    }

    // Practice share card dialog
    // Image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onTakePhoto = {
                showImagePicker = false
                val hasCamPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCamPermission) {
                    val photoFile = java.io.File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                    cameraImageUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onChooseFromGallery = {
                showImagePicker = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Template picker dialog
    if (showTemplatePicker) {
        com.proactivediary.ui.templates.TemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onTemplateSelected = { template ->
                showTemplatePicker = false
                viewModel.applyTemplate(template.id, template.prompts)
            }
        )
    }

    if (streakShareCount > 0) {
        val milestone = when (streakShareCount) {
            7 -> "One week of practice"
            14 -> "Two weeks of practice"
            21 -> "Three weeks of practice"
            30 -> "One month of practice"
            50 -> "Fifty days of practice"
            100 -> "One hundred days of practice"
            365 -> "One year of practice"
            else -> "Day $streakShareCount of practice"
        }
        val shareData = ShareCardData(
            excerpt = "Day $streakShareCount of my writing practice.",
            title = milestone,
            colorKey = state.colorKey
        )
        ShareCardDialog(
            data = shareData,
            onDismiss = { streakShareCount = 0 },
            onShare = { bitmap ->
                shareCardAsImage(context, bitmap)
                streakShareCount = 0
            }
        )
    }

    // Full-screen image viewer
    viewingImageId?.let { imageId ->
        val image = state.images.find { it.id == imageId }
        if (image != null) {
            val imageFile = viewModel.imageStorageManager
                .getImageFile(state.entryId, image.filename)
            ImageViewer(
                imageFile = imageFile,
                onDismiss = { viewingImageId = null }
            )
        }
    }

    // Suggestions bottom sheet
    if (showSuggestions) {
        SuggestionsBottomSheet(
            onDismiss = { showSuggestions = false },
            onSuggestionSelected = { suggestion ->
                showSuggestions = false
                viewModel.onContentChanged(suggestion.prompt + "\n\n")
            },
            textColor = textColor,
            secondaryTextColor = secondaryTextColor,
            backgroundColor = bgColor
        )
    }
}

@Composable
private fun GuidedPromptBanner(
    prompt: String,
    currentIndex: Int,
    totalPrompts: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = prompt,
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1} of $totalPrompts",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    color = secondaryTextColor.copy(alpha = 0.4f)
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentIndex < totalPrompts - 1) {
                    Text(
                        text = "Next",
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = secondaryTextColor.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.clickable(onClick = onNext)
                    )
                }
                Text(
                    text = "Dismiss",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = secondaryTextColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteArea(
    content: String,
    onContentChanged: (String) -> Unit,
    richTextState: RichTextState? = null,
    canvas: String,
    textColor: Color,
    secondaryTextColor: Color,
    horizontalPadding: Dp,
    fontSizeSp: Int,
    lineHeightMultiplier: Float,
    placeholderText: String = "Start writing...",
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

        if (richTextState != null) {
            // Rich text editor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = leftPadding, end = horizontalPadding)
            ) {
                if (richTextState.annotatedString.text.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontStyle = FontStyle.Italic,
                            fontSize = fontSize,
                            color = secondaryTextColor.copy(alpha = 0.35f),
                            lineHeight = lineHeightSp
                        )
                    )
                }
                RichTextEditor(
                    state = richTextState,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = fontSize,
                        color = textColor,
                        lineHeight = lineHeightSp
                    ),
                    colors = RichTextEditorDefaults.richTextEditorColors(
                        containerColor = Color.Transparent,
                        cursorColor = textColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Fallback plain text editor
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
                                text = placeholderText,
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontStyle = FontStyle.Italic,
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
}

@Composable
private fun SavedIndicator(
    isSaving: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    var showSaved by remember { mutableStateOf(false) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(isSaving) {
        if (isSaving) {
            showSaved = true
            alpha.snapTo(0f)
            alpha.animateTo(1f, animationSpec = tween(200))
        } else if (showSaved) {
            delay(2000)
            alpha.animateTo(0f, animationSpec = tween(400))
            showSaved = false
        }
    }

    if (showSaved || alpha.value > 0f) {
        Text(
            text = "Saved",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 12.sp,
                color = color.copy(alpha = alpha.value)
            ),
            modifier = modifier
        )
    }
}
