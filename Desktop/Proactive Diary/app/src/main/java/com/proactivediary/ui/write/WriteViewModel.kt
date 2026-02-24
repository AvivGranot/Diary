package com.proactivediary.ui.write

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.analytics.AnalyticsService
import android.net.Uri
import com.proactivediary.data.db.dao.InsightDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.location.LocationService
import com.proactivediary.data.media.ImageMetadata
import com.proactivediary.data.media.ImageStorageManager
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.JournalRepository
import com.proactivediary.data.weather.WeatherService
import com.proactivediary.data.repository.StreakRepository
import com.proactivediary.notifications.NotificationService
import com.proactivediary.domain.WritingGoalService
import com.proactivediary.domain.recommendations.NearbyPlace
import com.proactivediary.domain.recommendations.LocationSuggestion
import com.proactivediary.domain.recommendations.RecommendationsProvider
import com.proactivediary.domain.recommendations.RecommendationsState
import com.proactivediary.domain.model.TaggedContact
import com.proactivediary.playstore.InAppReviewService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class JournalPickerItem(
    val id: String,
    val title: String,
    val emoji: String?
)

object WritingPrompts {
    val prompts = listOf(
        "Even one word counts.",
        "How are you? Really.",
        "One sentence is enough.",
        "What's on your mind right now?",
        "One thing you're grateful for today.",
        "Just a word. That's all you need.",
        "What would you tell yourself a year from now?",
        "Describe a moment that made you feel something today.",
        "What's something you're putting off? Why?",
        "Write about someone who matters to you.",
        "What did you learn today?",
        "If today had a color, what would it be and why?",
        "What's a small win from today?",
        "Write the first thing that comes to mind. Don't stop for 2 minutes.",
        "What are you looking forward to?",
        "Describe the last time you laughed hard.",
        "What's a belief you've changed recently?",
        "Write a letter to your future self.",
        "What does your ideal morning look like?",
        "What's the bravest thing you did recently?",
        "Describe how you're feeling right now.",
        "What would you do if you weren't afraid?",
        "Write about a place that feels like home.",
        "What's one thing you'd change about today?",
        "What's a memory that always makes you smile?",
        "What are you avoiding thinking about?",
        "Describe a conversation that stuck with you.",
        "What's something you're proud of but never talk about?",
        "If you could relive one day, which would it be?",
        "What does success look like to you right now?",
        "Write about someone you miss.",
        "What's a question you wish someone would ask you?",
        "Describe your happiest moment this week.",
        "What advice would you give your younger self?",
        "What's making you feel alive lately?",
        "Write about something beautiful you noticed today."
    )

    fun dailyPrompt(): String {
        val dayOfYear = LocalDate.now().dayOfYear
        return prompts[dayOfYear % prompts.size]
    }
}

data class WriteUiState(
    val entryId: String = "",
    val title: String = "",
    val content: String = "",
    val contentHtml: String? = null,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isLoaded: Boolean = false,
    val dateHeader: String = "",
    val dailyPrompt: String = WritingPrompts.dailyPrompt(),
    val colorKey: String = "paper",
    val form: String = "focused",
    val texture: String = "paper",
    val canvas: String = "lined",
    val fontSize: Int = 16,
    val features: List<String> = listOf("auto_save", "word_count", "date_header", "daily_quote"),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif",
    val isNewEntry: Boolean = true,
    val showDesignStudioPrompt: Boolean = false,
    val goalCompletedMessage: String? = null,
    val weeklyGoalProgress: String? = null,
    val taggedContacts: List<TaggedContact> = emptyList(),
    val images: List<ImageMetadata> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val weatherTemp: Double? = null,
    val weatherCondition: String? = null,
    val weatherIcon: String? = null,
    val templateId: String? = null,
    val templatePrompts: List<String> = emptyList(),
    val currentPromptIndex: Int = 0,
    val showStreakCelebration: Int = 0,
    val newEntrySaved: Boolean = false,
    val requestInAppReview: Boolean = false,
    val showFirstEntryCelebration: Boolean = false,
    val audioPath: String? = null,
    val isDictating: Boolean = false,
    val partialTranscript: String = "",
    val speechAvailable: Boolean = false,
    val isFirstEverWrite: Boolean = false,
    val fontColor: String? = null,
    val showFocusTimer: Boolean = false,
    val focusTimerRunning: Boolean = false,
    val focusTimerSeconds: Int = 300,
    val showTimeCapsulePicker: Boolean = false,
    val weeklyConsistencyLabel: String? = null,
    val compassionateStreakMessage: String? = null,
    val compassionateSaveMessage: String? = null,
    val selectedJournalId: String? = null,
    val selectedJournalName: String? = null,
    val availableJournals: List<JournalPickerItem> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class WriteViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val preferenceDao: PreferenceDao,
    private val analyticsService: AnalyticsService,
    private val writingGoalService: WritingGoalService,
    private val streakRepository: StreakRepository,
    private val inAppReviewService: InAppReviewService,
    val imageStorageManager: ImageStorageManager,
    private val speechToTextService: com.proactivediary.data.media.SpeechToTextService,
    private val locationService: LocationService,
    private val weatherService: WeatherService,
    private val insightDao: InsightDao,
    private val recommendationsProvider: RecommendationsProvider,
    private val notificationService: NotificationService,
    private val journalRepository: JournalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    private val _contentFlow = MutableStateFlow("")
    private var titleSaveJob: Job? = null
    private var focusTimerJob: Job? = null
    private val _transcribedTextFlow = MutableSharedFlow<String>(extraBufferCapacity = 20)
    val transcribedTextFlow: SharedFlow<String> = _transcribedTextFlow

    private val _recommendations = MutableStateFlow(RecommendationsState())
    val recommendations: StateFlow<RecommendationsState> = _recommendations.asStateFlow()

    private val gson = Gson()
    private val sessionStartMs = System.currentTimeMillis()

    private val entryIdArg: String? = savedStateHandle.get<String>("entryId")

    private val themeKeys = listOf(
        "diary_color", "diary_form", "diary_texture", "diary_canvas",
        "diary_details", "diary_mark_text", "diary_mark_position",
        "diary_mark_font", "font_size"
    )

    private val defaultFeatures = listOf("auto_save", "word_count", "date_header", "daily_quote")

    init {
        observeThemePreferences()
        loadOrCreateEntry(entryIdArg)
        setupAutoSave()
        loadGoalProgress()
        loadSmartPrompt()
        loadWeeklyConsistency()
        loadJournals()
        analyticsService.logWriteScreenViewed()
        loadRecommendations()

        val pacificZone = java.time.ZoneId.of("America/Los_Angeles")
        val today = LocalDate.now(pacificZone)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
        _uiState.value = _uiState.value.copy(dateHeader = today.format(formatter))

        // Auto-fetch location and weather for new entries
        if (entryIdArg == null) {
            fetchLocationAndWeather()
        }

        // Check if this is the user's very first write (for auto-showing templates)
        if (entryIdArg == null) {
            viewModelScope.launch {
                val logged = preferenceDao.get("first_entry_logged")?.value
                val templateShown = preferenceDao.get("template_prompt_shown")?.value
                if (logged == null && templateShown == null) {
                    _uiState.value = _uiState.value.copy(isFirstEverWrite = true)
                }
            }
        }

        // Speech-to-text setup
        speechToTextService.checkAvailability()
        viewModelScope.launch {
            speechToTextService.isAvailable.collect { available ->
                _uiState.update { it.copy(speechAvailable = available) }
            }
        }
        viewModelScope.launch {
            speechToTextService.partialText.collect { partial ->
                _uiState.update { it.copy(partialTranscript = partial) }
            }
        }
        viewModelScope.launch {
            speechToTextService.finalText.collect { text ->
                _transcribedTextFlow.emit(text)
            }
        }
        viewModelScope.launch {
            speechToTextService.error.collect { errorMsg ->
                _uiState.update { it.copy(isDictating = false) }
                _speechError.emit(errorMsg)
            }
        }
    }

    private val _speechError = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val speechError: SharedFlow<String> = _speechError.asSharedFlow()

    override fun onCleared() {
        super.onCleared()
        focusTimerJob?.cancel()
        // Cancel any pending debounced title save — we'll force-save below
        titleSaveJob?.cancel()
        // Force-save any pending title/content changes before ViewModel is destroyed
        // (e.g., user switches tabs before debounce completes)
        val state = _uiState.value
        if (state.isLoaded && (state.content.isNotBlank() || state.title.isNotBlank())) {
            runBlocking(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val tagsJson = gson.toJson(state.tags)
                val contactsJson = gson.toJson(state.taggedContacts)
                val imagesJson = gson.toJson(state.images)
                val entry = EntryEntity(
                    id = state.entryId,
                    title = state.title,
                    content = state.content,
                    contentHtml = state.contentHtml,
                    contentPlain = state.content,
                    mood = null,
                    tags = tagsJson,
                    taggedContacts = contactsJson,
                    images = imagesJson,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationName = state.locationName,
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition,
                    weatherIcon = state.weatherIcon,
                    templateId = state.templateId,
                    audioPath = state.audioPath,
                    fontColor = state.fontColor,
                    wordCount = state.wordCount,
                    createdAt = entryRepository.getByIdSync(state.entryId)?.createdAt
                        ?: now,
                    updatedAt = now
                )
                if (state.isNewEntry) {
                    entryRepository.insert(entry)
                } else {
                    entryRepository.update(entry)
                }
            }
        }
    }

    private fun observeThemePreferences() {
        viewModelScope.launch {
            preferenceDao.observeBatch(themeKeys).collect { entities ->
                val prefs = entities.associate { it.key to it.value }

                val detailsJson = prefs["diary_details"]
                val features: List<String> = if (detailsJson != null) {
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson(detailsJson, type)
                    } catch (_: Exception) { defaultFeatures }
                } else { defaultFeatures }

                _uiState.value = _uiState.value.copy(
                    colorKey = prefs["diary_color"] ?: "paper",
                    form = prefs["diary_form"] ?: "focused",
                    texture = prefs["diary_texture"] ?: "paper",
                    canvas = prefs["diary_canvas"] ?: "lined",
                    fontSize = when (prefs["font_size"]) {
                        "small" -> 14; "large" -> 18; else -> 16
                    },
                    features = features,
                    markText = prefs["diary_mark_text"] ?: "",
                    markPosition = prefs["diary_mark_position"] ?: "header",
                    markFont = prefs["diary_mark_font"] ?: "serif"
                )
            }
        }
    }

    fun loadOrCreateEntry(entryId: String?) {
        viewModelScope.launch {
            if (entryId != null) {
                val entry = entryRepository.getByIdSync(entryId)
                if (entry != null) {
                    val tags = parseTags(entry.tags)
                    val contacts = parseTaggedContacts(entry.taggedContacts)
                    val images = parseImages(entry.images)
                    _uiState.value = _uiState.value.copy(
                        entryId = entry.id,
                        title = entry.title,
                        content = entry.content,
                        contentHtml = entry.contentHtml,
                        tags = tags,
                        taggedContacts = contacts,
                        images = images,
                        latitude = entry.latitude,
                        longitude = entry.longitude,
                        locationName = entry.locationName,
                        weatherTemp = entry.weatherTemp,
                        weatherCondition = entry.weatherCondition,
                        weatherIcon = entry.weatherIcon,
                        audioPath = entry.audioPath,
                        fontColor = entry.fontColor,
                        wordCount = computeWordCount(entry.content),
                        isLoaded = true,
                        isNewEntry = false
                    )
                    _contentFlow.value = entry.content

                    // Load journal assignment for existing entry
                    val journalIds = journalRepository.getJournalIdsForEntry(entry.id)
                    val firstJournalId = journalIds.firstOrNull()
                    if (firstJournalId != null) {
                        val journal = journalRepository.getById(firstJournalId)
                        _uiState.update {
                            it.copy(
                                selectedJournalId = firstJournalId,
                                selectedJournalName = journal?.title
                            )
                        }
                    }
                } else {
                    // Entry was deleted — fall through to create a new one
                    val newId = UUID.randomUUID().toString()
                    _uiState.value = _uiState.value.copy(
                        entryId = newId,
                        isLoaded = true,
                        isNewEntry = true
                    )
                    _contentFlow.value = ""
                }
            } else {
                val todayEntry = entryRepository.getTodayEntrySync()
                if (todayEntry != null) {
                    val tags = parseTags(todayEntry.tags)
                    val contacts = parseTaggedContacts(todayEntry.taggedContacts)
                    val images = parseImages(todayEntry.images)
                    _uiState.value = _uiState.value.copy(
                        entryId = todayEntry.id,
                        title = todayEntry.title,
                        content = todayEntry.content,
                        contentHtml = todayEntry.contentHtml,
                        tags = tags,
                        taggedContacts = contacts,
                        images = images,
                        latitude = todayEntry.latitude,
                        longitude = todayEntry.longitude,
                        locationName = todayEntry.locationName,
                        weatherTemp = todayEntry.weatherTemp,
                        weatherCondition = todayEntry.weatherCondition,
                        weatherIcon = todayEntry.weatherIcon,
                        audioPath = todayEntry.audioPath,
                        fontColor = todayEntry.fontColor,
                        wordCount = computeWordCount(todayEntry.content),
                        isLoaded = true,
                        isNewEntry = false
                    )
                    _contentFlow.value = todayEntry.content
                } else {
                    val newId = UUID.randomUUID().toString()
                    _uiState.value = _uiState.value.copy(
                        entryId = newId,
                        title = "",
                        content = "",
                        tags = emptyList(),
                        wordCount = 0,
                        isLoaded = true,
                        isNewEntry = true
                    )
                    _contentFlow.value = ""

                }
            }
        }
    }

    private fun setupAutoSave() {
        viewModelScope.launch {
            _contentFlow
                .debounce(5000)
                .collect { text ->
                    val state = _uiState.value
                    if (text.isBlank() && state.title.isBlank()) {
                        // User cleared everything — delete entry if previously saved
                        if (!state.isNewEntry) {
                            try {
                                withContext(Dispatchers.IO) {
                                    entryRepository.delete(state.entryId)
                                }
                            } catch (_: Exception) {}
                            val newId = UUID.randomUUID().toString()
                            _uiState.update { it.copy(entryId = newId, isNewEntry = true) }
                        }
                    } else {
                        saveEntry(text)
                    }
                }
        }
    }

    private var firstEntryStartLogged = false

    fun onContentChanged(newContent: String) {
        val state = _uiState.value
        // Log when a first-time user starts writing (content goes from empty to non-empty)
        if (!firstEntryStartLogged && state.isNewEntry && state.content.isEmpty() && newContent.isNotEmpty()) {
            viewModelScope.launch {
                val logged = preferenceDao.get("first_entry_logged")?.value
                if (logged == null) {
                    analyticsService.logFirstEntryStarted(computeWordCount(newContent))
                }
            }
            firstEntryStartLogged = true
        }
        _uiState.value = state.copy(
            content = newContent,
            wordCount = computeWordCount(newContent)
        )
        _contentFlow.value = newContent
    }

    fun onRichContentChanged(html: String, plainText: String) {
        val state = _uiState.value
        if (!firstEntryStartLogged && state.isNewEntry && state.content.isEmpty() && plainText.isNotEmpty()) {
            viewModelScope.launch {
                val logged = preferenceDao.get("first_entry_logged")?.value
                if (logged == null) {
                    analyticsService.logFirstEntryStarted(computeWordCount(plainText))
                }
            }
            firstEntryStartLogged = true
        }
        _uiState.value = state.copy(
            content = plainText,
            contentHtml = html,
            wordCount = computeWordCount(plainText)
        )
        _contentFlow.value = plainText
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
        // MutableStateFlow deduplicates — re-emitting same content won't trigger auto-save.
        // Use a separate debounced job so title-only changes are always persisted.
        if (_uiState.value.content.isNotBlank() || newTitle.isNotBlank()) {
            titleSaveJob?.cancel()
            titleSaveJob = viewModelScope.launch {
                delay(500)
                saveEntry(_uiState.value.content)
            }
        }
    }

    fun onTagsUpdated(tags: List<String>) {
        _uiState.value = _uiState.value.copy(tags = tags)
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun onContactTagged(contact: TaggedContact) {
        val current = _uiState.value.taggedContacts
        if (current.any { it.lookupUri == contact.lookupUri }) return
        _uiState.value = _uiState.value.copy(taggedContacts = current + contact)
        analyticsService.logContactTagged(current.size + 1)
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun onContactRemoved(contact: TaggedContact) {
        _uiState.value = _uiState.value.copy(
            taggedContacts = _uiState.value.taggedContacts.filter { it.lookupUri != contact.lookupUri }
        )
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun saveNow() {
        val state = _uiState.value
        if (state.content.isNotBlank() || state.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(state.content) }
        }
    }

    /**
     * Save entry even when title/content are blank, as long as images exist.
     * Ensures drawings and photos on blank entries are persisted to Room.
     */
    private suspend fun forceSaveWithImages() {
        val state = _uiState.value
        if (state.images.isEmpty()) {
            saveEntry(state.content)
            return
        }
        // Images exist — force save regardless of blank content
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        try {
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val tagsJson = gson.toJson(state.tags)
                val contactsJson = gson.toJson(state.taggedContacts)
                val imagesJson = gson.toJson(state.images)
                val entry = EntryEntity(
                    id = state.entryId,
                    title = state.title,
                    content = state.content,
                    contentHtml = state.contentHtml,
                    contentPlain = state.content,
                    mood = null,
                    tags = tagsJson,
                    taggedContacts = contactsJson,
                    images = imagesJson,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationName = state.locationName,
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition,
                    weatherIcon = state.weatherIcon,
                    templateId = state.templateId,
                    audioPath = state.audioPath,
                    fontColor = state.fontColor,
                    wordCount = computeWordCount(state.content),
                    createdAt = if (state.isNewEntry) now else {
                        entryRepository.getByIdSync(state.entryId)?.createdAt ?: now
                    },
                    updatedAt = now
                )
                if (state.isNewEntry) {
                    entryRepository.insert(entry)
                } else {
                    entryRepository.update(entry)
                }
            }
            _uiState.update { it.copy(isSaving = false, isNewEntry = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, saveError = "Failed to save entry.") }
        }
    }

    fun clearSaveError() {
        _uiState.value = _uiState.value.copy(saveError = null)
    }

    /** Returns true if there are unsaved changes worth warning about. */
    fun hasUnsavedContent(): Boolean {
        val state = _uiState.value
        return state.isNewEntry && (state.content.isNotBlank() || state.title.isNotBlank())
    }

    private suspend fun saveEntry(text: String) {
        val state = _uiState.value
        // Don't save completely empty entries
        if (text.isBlank() && state.title.isBlank()) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        try {
            // Collect results from IO, then apply state changes atomically on return
            data class SaveResult(
                val isFirstEntry: Boolean = false,
                val showDesignPrompt: Boolean = false,
                val goalMessage: String? = null,
                val goalProgress: String? = null,
                val streakMilestone: Int = 0
            )

            val result = withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val tagsJson = gson.toJson(state.tags)
                val contactsJson = gson.toJson(state.taggedContacts)
                val imagesJson = gson.toJson(state.images)

                val entry = EntryEntity(
                    id = state.entryId,
                    title = state.title,
                    content = text,
                    contentHtml = state.contentHtml,
                    contentPlain = text,
                    mood = null,
                    tags = tagsJson,
                    taggedContacts = contactsJson,
                    images = imagesJson,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationName = state.locationName,
                    weatherTemp = state.weatherTemp,
                    weatherCondition = state.weatherCondition,
                    weatherIcon = state.weatherIcon,
                    templateId = state.templateId,
                    audioPath = state.audioPath,
                    fontColor = state.fontColor,
                    wordCount = computeWordCount(text),
                    createdAt = if (state.isNewEntry) now else {
                        entryRepository.getByIdSync(state.entryId)?.createdAt ?: now
                    },
                    updatedAt = now
                )

                var isFirstEntry = false
                var showDesignPrompt = false
                var goalMessage: String? = null
                var goalProgress: String? = null
                var streakMilestone = 0

                if (state.isNewEntry) {
                    entryRepository.insert(entry)

                    val sessionDuration = System.currentTimeMillis() - sessionStartMs
                    analyticsService.logEntrySaved(
                        wordCount = computeWordCount(text),
                        hasTitle = state.title.isNotBlank(),
                        sessionDurationMs = sessionDuration
                    )

                    val localNow = java.time.LocalDateTime.now()
                    analyticsService.logWritingPattern(
                        dayOfWeek = localNow.dayOfWeek.name.lowercase(),
                        hourOfDay = localNow.hour,
                        wordCount = computeWordCount(text)
                    )

                    val firstEntryLogged = preferenceDao.get("first_entry_logged")?.value
                    if (firstEntryLogged == null) {
                        val installTime = preferenceDao.get("trial_start_date")?.value?.toLongOrNull()
                            ?: System.currentTimeMillis()
                        val timeSinceInstall = System.currentTimeMillis() - installTime
                        analyticsService.logFirstEntryWritten(
                            wordCount = computeWordCount(text),
                            timeSinceInstallMs = timeSinceInstall
                        )
                        preferenceDao.insert(PreferenceEntity("first_entry_logged", "true"))
                        isFirstEntry = true
                    }

                    val designDone = preferenceDao.get("design_studio_completed")?.value == "true"
                    val designLegacy = preferenceDao.get("design_completed")?.value == "true"
                    showDesignPrompt = !designDone && !designLegacy

                    val checkedInGoals = writingGoalService.autoCheckInWritingGoals()
                    if (checkedInGoals.isNotEmpty()) {
                        goalMessage = "Goal complete!"
                        goalProgress = writingGoalService.getWritingGoalProgress()
                    }

                    inAppReviewService.incrementEntryCount()

                    val streak = streakRepository.calculateWritingStreak()
                    if (isMilestone(streak)) {
                        streakMilestone = streak
                    }

                    // Assign to journal if selected
                    if (state.selectedJournalId != null) {
                        journalRepository.addEntryToJournal(state.selectedJournalId!!, state.entryId)
                    }

                    // Check for time capsule template trigger
                    // (handled below via showTimeCapsulePicker state)
                } else {
                    entryRepository.update(entry)

                    val checkedInGoals = writingGoalService.autoCheckInWritingGoals()
                    if (checkedInGoals.isNotEmpty()) {
                        goalMessage = "Goal complete!"
                        goalProgress = writingGoalService.getWritingGoalProgress()
                    }
                }

                SaveResult(isFirstEntry, showDesignPrompt, goalMessage, goalProgress, streakMilestone)
            }

            // Check if this is a capsule-eligible template (show picker on first save)
            val isCapsuleTemplate = state.isNewEntry && state.templateId in listOf(
                "tpl_letter_to_self", "tpl_inner_child_letter"
            )

            // Apply all state changes atomically back on the calling dispatcher
            _uiState.update { current ->
                current.copy(
                    isNewEntry = if (state.isNewEntry) false else current.isNewEntry,
                    isSaving = false,
                    newEntrySaved = state.isNewEntry || current.newEntrySaved,
                    showFirstEntryCelebration = result.isFirstEntry || current.showFirstEntryCelebration,
                    showDesignStudioPrompt = result.showDesignPrompt || current.showDesignStudioPrompt,
                    goalCompletedMessage = result.goalMessage ?: current.goalCompletedMessage,
                    weeklyGoalProgress = result.goalProgress ?: current.weeklyGoalProgress,
                    showStreakCelebration = if (result.streakMilestone > 0) result.streakMilestone else current.showStreakCelebration,
                    showTimeCapsulePicker = isCapsuleTemplate || current.showTimeCapsulePicker
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, saveError = "Failed to save entry. Please try again.") }
        }
    }

    fun addImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val metadata = imageStorageManager.saveImage(state.entryId, uri)
                _uiState.update { it.copy(images = it.images + metadata) }
                forceSaveWithImages()
                analyticsService.logFeatureUsed("photo_attached")
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = "Failed to attach photo.") }
            }
        }
    }

    fun removeImage(imageId: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val image = state.images.find { it.id == imageId } ?: return@launch
            imageStorageManager.deleteImage(state.entryId, image.filename)
            _uiState.update { it.copy(images = it.images.filter { img -> img.id != imageId }) }
            // Trigger save to persist images JSON
            saveEntry(_uiState.value.content)
            analyticsService.logFeatureUsed("photo_removed")
        }
    }

    fun updateFontColor(hex: String?) {
        _uiState.update { it.copy(fontColor = hex) }
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
        }
    }

    fun addDrawingBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val drawingId = UUID.randomUUID().toString()
                val filename = "drawing_$drawingId.png"
                val dir = imageStorageManager.getEntryImagesDir(state.entryId)
                withContext(Dispatchers.IO) {
                    dir.mkdirs()
                    val file = java.io.File(dir, filename)
                    java.io.FileOutputStream(file).use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                val metadata = ImageMetadata(
                    id = drawingId,
                    filename = filename,
                    width = bitmap.width,
                    height = bitmap.height,
                    addedAt = System.currentTimeMillis()
                )
                _uiState.update { it.copy(images = it.images + metadata) }
                forceSaveWithImages()
                analyticsService.logFeatureUsed("drawing_added")
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = "Failed to save drawing.") }
            }
        }
    }

    fun startDictation() {
        if (!_uiState.value.speechAvailable) {
            viewModelScope.launch {
                _speechError.emit("Speech recognition is not available on this device")
            }
            return
        }
        speechToTextService.startListening()
        _uiState.update { it.copy(isDictating = true) }
        analyticsService.logFeatureUsed("dictation_started")
    }

    fun stopDictation() {
        speechToTextService.stopListening()
        _uiState.update { it.copy(isDictating = false, partialTranscript = "") }
        analyticsService.logFeatureUsed("dictation_completed")
    }

    private fun parseImages(json: String): List<ImageMetadata> {
        return try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseTags(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseTaggedContacts(json: String): List<TaggedContact> {
        return try {
            val type = object : TypeToken<List<TaggedContact>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun computeWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
    }

    fun dismissDesignStudioPrompt() {
        _uiState.value = _uiState.value.copy(showDesignStudioPrompt = false)
    }

    fun dismissGoalCompleted() {
        _uiState.value = _uiState.value.copy(goalCompletedMessage = null)
    }

    fun dismissStreakCelebration() {
        _uiState.value = _uiState.value.copy(
            showStreakCelebration = 0,
            requestInAppReview = true
        )
    }

    fun clearInAppReviewRequest() {
        _uiState.value = _uiState.value.copy(requestInAppReview = false)
    }

    fun requestInAppReview(activity: android.app.Activity) {
        viewModelScope.launch {
            inAppReviewService.maybeRequestReview(activity)
            clearInAppReviewRequest()
        }
    }

    fun dismissFirstEntryCelebration() {
        _uiState.value = _uiState.value.copy(showFirstEntryCelebration = false)
    }

    fun sealAsCapsule(openDateMs: Long) {
        viewModelScope.launch {
            val entryId = _uiState.value.entryId
            withContext(Dispatchers.IO) {
                entryRepository.setCapsuleOpenDate(entryId, openDateMs)
                notificationService.scheduleTimeCapsuleAlarm(entryId, openDateMs)
            }
            _uiState.update { it.copy(showTimeCapsulePicker = false) }
        }
    }

    fun dismissTimeCapsulePicker() {
        _uiState.update { it.copy(showTimeCapsulePicker = false) }
    }

    private fun loadJournals() {
        viewModelScope.launch {
            journalRepository.getAll().collect { journals ->
                _uiState.update {
                    it.copy(
                        availableJournals = journals.map { j ->
                            JournalPickerItem(id = j.id, title = j.title, emoji = j.emoji)
                        }
                    )
                }
            }
        }
    }

    fun assignToJournal(journalId: String?) {
        val journal = _uiState.value.availableJournals.find { it.id == journalId }
        _uiState.update {
            it.copy(
                selectedJournalId = journalId,
                selectedJournalName = journal?.title
            )
        }
    }

    // --- Focus Timer ---

    fun showFocusTimerOverlay() {
        _uiState.update { it.copy(showFocusTimer = true) }
    }

    fun startFocusTimer() {
        _uiState.update { it.copy(showFocusTimer = false, focusTimerRunning = true, focusTimerSeconds = 300) }
        focusTimerJob = viewModelScope.launch {
            while (_uiState.value.focusTimerSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(focusTimerSeconds = it.focusTimerSeconds - 1) }
            }
            // Timer done — UI handles "Well done!" display and calls dismissFocusTimer
        }
    }

    fun skipFocusTimer() {
        _uiState.update { it.copy(showFocusTimer = false) }
    }

    fun dismissFocusTimer() {
        focusTimerJob?.cancel()
        _uiState.update { it.copy(focusTimerRunning = false, focusTimerSeconds = 300) }
    }

    private fun loadGoalProgress() {
        viewModelScope.launch {
            val progress = writingGoalService.getWritingGoalProgress()
            if (progress != null) {
                _uiState.value = _uiState.value.copy(weeklyGoalProgress = progress)
            }
        }
    }

    private fun loadWeeklyConsistency() {
        viewModelScope.launch {
            try {
                val label = withContext(Dispatchers.IO) {
                    streakRepository.getWeeklyConsistencyLabel()
                }
                val message = withContext(Dispatchers.IO) {
                    streakRepository.getCompassionateMessage()
                }
                _uiState.update {
                    it.copy(
                        weeklyConsistencyLabel = label,
                        compassionateStreakMessage = message
                    )
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Quick capture: saves a minimal entry with just content.
     */
    fun saveQuickThought(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val entry = EntryEntity(
                    id = UUID.randomUUID().toString(),
                    title = "",
                    content = text,
                    contentPlain = text,
                    wordCount = computeWordCount(text),
                    createdAt = now,
                    updatedAt = now
                )
                entryRepository.insert(entry)
            }
        }
    }

    /**
     * Compassionate save message based on word count.
     */
    fun getCompassionateSaveMessage(wordCount: Int): String {
        return when {
            wordCount <= 1 -> "that\u2019s a start"
            wordCount <= 5 -> "every word counts"
            wordCount <= 10 -> "that\u2019s all you need"
            wordCount <= 30 -> "nice"
            wordCount <= 50 -> "you\u2019re rolling"
            else -> "you\u2019re in the flow"
        }
    }

    fun markTemplatePromptShown() {
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("template_prompt_shown", "true"))
            _uiState.value = _uiState.value.copy(isFirstEverWrite = false)
        }
    }

    private fun loadSmartPrompt() {
        viewModelScope.launch {
            try {
                val aiEnabled = preferenceDao.getValue("ai_insights_enabled") == "true"
                if (!aiEnabled) return@launch

                insightDao.getLatestInsight().collect { insight ->
                    if (insight != null) {
                        val type = object : TypeToken<List<String>>() {}.type
                        val aiPrompts: List<String> = try {
                            gson.fromJson(insight.promptSuggestions, type) ?: emptyList()
                        } catch (_: Exception) { emptyList() }

                        if (aiPrompts.isNotEmpty()) {
                            val dayIndex = LocalDate.now().dayOfYear % aiPrompts.size
                            _uiState.update {
                                it.copy(dailyPrompt = aiPrompts[dayIndex])
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to static prompts (already set as default)
            }
        }
    }

    fun applyTemplate(templateId: String, prompts: List<String>) {
        _uiState.update {
            it.copy(
                templateId = templateId,
                templatePrompts = prompts,
                currentPromptIndex = 0
            )
        }
        analyticsService.logFeatureUsed("template_used")
    }

    fun advancePrompt() {
        val state = _uiState.value
        if (state.currentPromptIndex < state.templatePrompts.size - 1) {
            _uiState.update { it.copy(currentPromptIndex = it.currentPromptIndex + 1) }
        }
    }

    fun dismissTemplate() {
        _uiState.update { it.copy(templatePrompts = emptyList(), currentPromptIndex = 0) }
    }

    /**
     * Activates a suggestion as a guided prompt banner above the editor.
     * Reuses the template prompt infrastructure — the prompt guides, it doesn't become content.
     */
    fun activateGuidedPrompt(prompt: String) {
        _uiState.update { it.copy(
            templatePrompts = listOf(prompt),
            currentPromptIndex = 0
        )}
        analyticsService.logFeatureUsed("suggestion_guided")
    }

    private fun fetchLocationAndWeather() {
        viewModelScope.launch {
            try {
                val locationData = withContext(Dispatchers.IO) { locationService.getLocation() }
                if (locationData != null) {
                    _uiState.update {
                        it.copy(
                            latitude = locationData.latitude,
                            longitude = locationData.longitude,
                            locationName = locationData.displayName
                        )
                    }
                    analyticsService.logFeatureUsed("location_captured")

                    // Load nearby places for recommendations
                    try {
                        val places = withContext(Dispatchers.IO) {
                            recommendationsProvider.getNearbyPlaces(locationData.latitude, locationData.longitude)
                        }
                        _recommendations.update { it.copy(nearbyPlaces = places) }
                    } catch (_: Exception) {}

                    // Chain weather fetch after location
                    val weather = withContext(Dispatchers.IO) {
                        weatherService.getWeather(locationData.latitude, locationData.longitude)
                    }
                    if (weather != null) {
                        _uiState.update {
                            it.copy(
                                weatherTemp = weather.temperature,
                                weatherCondition = weather.condition,
                                weatherIcon = weather.icon
                            )
                        }
                        analyticsService.logFeatureUsed("weather_captured")
                    }
                }
            } catch (_: Exception) {
                // Location/weather are optional — silently fail
            }
        }
    }

    fun logContactShared(hasEmail: Boolean, hasPhone: Boolean) {
        analyticsService.logContactShared(hasEmail, hasPhone)
    }

    fun hasFeature(feature: String): Boolean {
        return feature in _uiState.value.features
    }

    fun clearNewEntrySaved() {
        _uiState.value = _uiState.value.copy(newEntrySaved = false)
    }

    /** Re-check if current entry still exists (e.g. after returning from journal detail delete). */
    fun refreshEntry() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isLoaded) return@launch
            if (!state.isNewEntry && state.entryId.isNotBlank()) {
                val exists = withContext(Dispatchers.IO) {
                    entryRepository.getByIdSync(state.entryId)
                }
                if (exists == null) {
                    _contentFlow.value = ""
                    loadOrCreateEntry(null)
                }
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _recommendations.update { it.copy(isLoading = true) }
            try {
                val recentEntries = recommendationsProvider.getRecentEntries()
                val pastLocations = recommendationsProvider.getRecentLocations()
                _recommendations.update {
                    it.copy(
                        recentEntries = recentEntries,
                        pastLocations = pastLocations,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _recommendations.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadDevicePhotos() {
        viewModelScope.launch {
            val photos = recommendationsProvider.getRecentDevicePhotos()
            _recommendations.update { it.copy(devicePhotos = photos, hasPhotoPermission = true) }
        }
    }

    fun onNearbyPlaceTapped(place: NearbyPlace) {
        _uiState.update {
            it.copy(
                latitude = place.lat,
                longitude = place.lng,
                locationName = place.name
            )
        }
        activateGuidedPrompt("Write about your visit to ${place.name}...")
    }

    fun onLocationSuggestionTapped(location: LocationSuggestion) {
        _uiState.update {
            it.copy(
                latitude = location.lat,
                longitude = location.lng,
                locationName = location.name
            )
        }
        activateGuidedPrompt("Write about ${location.name}...")
    }
}
