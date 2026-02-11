package com.proactivediary.ui.write

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.repository.StreakRepository
import com.proactivediary.domain.WritingGoalService
import com.proactivediary.domain.model.Mood
import com.proactivediary.domain.model.TaggedContact
import com.proactivediary.playstore.InAppReviewService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

object WritingPrompts {
    val prompts = listOf(
        "What's on your mind right now?",
        "One thing you're grateful for today.",
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
        "Describe your current mood in detail.",
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
    val mood: Mood? = null,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isLoaded: Boolean = false,
    val dateHeader: String = "",
    val dailyPrompt: String = WritingPrompts.dailyPrompt(),
    val colorKey: String = "cream",
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
    val showStreakCelebration: Int = 0,
    val newEntrySaved: Boolean = false,
    val requestInAppReview: Boolean = false,
    val showFirstEntryCelebration: Boolean = false
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    private val _contentFlow = MutableStateFlow("")
    private var titleSaveJob: Job? = null
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
        analyticsService.logWriteScreenViewed()

        val estZone = java.time.ZoneId.of("America/New_York")
        val today = LocalDate.now(estZone)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
        _uiState.value = _uiState.value.copy(dateHeader = today.format(formatter))
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
                    colorKey = prefs["diary_color"] ?: "cream",
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
                    _uiState.value = _uiState.value.copy(
                        entryId = entry.id,
                        title = entry.title,
                        content = entry.content,
                        mood = Mood.fromString(entry.mood),
                        tags = tags,
                        taggedContacts = contacts,
                        wordCount = computeWordCount(entry.content),
                        isLoaded = true,
                        isNewEntry = false
                    )
                    _contentFlow.value = entry.content
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
                    _uiState.value = _uiState.value.copy(
                        entryId = todayEntry.id,
                        title = todayEntry.title,
                        content = todayEntry.content,
                        mood = Mood.fromString(todayEntry.mood),
                        tags = tags,
                        taggedContacts = contacts,
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
                        mood = null,
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

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
        // MutableStateFlow deduplicates — re-emitting same content won't trigger auto-save.
        // Use a separate debounced job so title-only changes are always persisted.
        if (_uiState.value.content.isNotBlank() || newTitle.isNotBlank()) {
            titleSaveJob?.cancel()
            titleSaveJob = viewModelScope.launch {
                delay(2000)
                saveEntry(_uiState.value.content)
            }
        }
    }

    fun onMoodSelected(mood: Mood?) {
        val currentMood = _uiState.value.mood
        _uiState.value = _uiState.value.copy(
            mood = if (currentMood == mood) null else mood
        )
        if (_uiState.value.content.isNotBlank() || _uiState.value.title.isNotBlank()) {
            viewModelScope.launch { saveEntry(_uiState.value.content) }
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

                val entry = EntryEntity(
                    id = state.entryId,
                    title = state.title,
                    content = text,
                    mood = state.mood?.key,
                    tags = tagsJson,
                    taggedContacts = contactsJson,
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
                        mood = state.mood?.key,
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
                            mood = state.mood?.key,
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
                    showStreakCelebration = if (result.streakMilestone > 0) result.streakMilestone else current.showStreakCelebration
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSaving = false, saveError = "Failed to save entry. Please try again.") }
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

    private fun loadGoalProgress() {
        viewModelScope.launch {
            val progress = writingGoalService.getWritingGoalProgress()
            if (progress != null) {
                _uiState.value = _uiState.value.copy(weeklyGoalProgress = progress)
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
}
