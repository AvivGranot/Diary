package com.proactivediary.ui.typewriter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TypewriterState {
    IDLE,
    TYPING,
    CURSOR_FADE,
    PAUSE_1,
    ATTRIBUTION,
    PAUSE_2,
    CALL_TO_ACTION,
    READY,
    NAVIGATING
}

data class TypewriterUiState(
    val state: TypewriterState = TypewriterState.IDLE,
    val visibleCharCount: Int = 0,
    val cursorVisible: Boolean = false,
    val cursorAlpha: Float = 1f,
    val attributionAlpha: Float = 0f,
    val ctaAlpha: Float = 0f,
    val ctaTranslateY: Float = 24f,
    val skipVisible: Boolean = true,
    val screenAlpha: Float = 1f,
    val isFirstLaunch: Boolean = true,
    val isNavigating: Boolean = false,
    val isLoaded: Boolean = false,
    val practiceDay: Int = 0,
    val practiceDayAlpha: Float = 0f
)

@HiltViewModel
class TypewriterViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val analyticsService: AnalyticsService,
    private val streakRepository: StreakRepository
) : ViewModel() {

    companion object {
        const val QUOTE = "If you would not be forgotten,\n" +
                "as soon as you are dead and rotten,\n" +
                "either write things worth reading,\n" +
                "or do things worth writing"
        const val ATTRIBUTION = "Benjamin Franklin"
        const val CTA = "Now write yours."

        private const val CHAR_DELAY_MS = 20L
        private const val INITIAL_DELAY_MS = 200L
        private const val CURSOR_HOLD_MS = 200L
        private const val CURSOR_FADE_MS = 200L
        private const val PAUSE_1_MS = 200L
        private const val ATTRIBUTION_FADE_MS = 500L
        private const val PAUSE_2_MS = 300L
        private const val CTA_SLIDE_MS = 400L
        private const val SKIP_APPEAR_MS = 0L
        private const val SCREEN_FADE_MS = 400L
        private const val RETURN_AUTO_ADVANCE_MS = 500L

        private const val PREF_KEY = "first_launch_completed"
    }

    private val _uiState = MutableStateFlow(TypewriterUiState())
    val uiState: StateFlow<TypewriterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadPracticeDay()
            checkFirstLaunch()
        }
    }

    private suspend fun loadPracticeDay() {
        val streak = streakRepository.calculateWritingStreak()
        // Practice day = streak count + 1 (today is the next day of practice)
        // For first-time users, show "1." — their first day
        val day = if (streak > 0) streak else 1
        _uiState.value = _uiState.value.copy(practiceDay = day)
    }

    private suspend fun checkFirstLaunch() {
        val pref = preferenceDao.get(PREF_KEY)
        val isFirst = pref?.value != "true"
        _uiState.value = _uiState.value.copy(
            isFirstLaunch = isFirst,
            isLoaded = true
        )
        if (isFirst) {
            startFirstLaunchSequence()
        } else {
            startReturnSequence()
        }
    }

    private suspend fun startFirstLaunchSequence() {
        _uiState.value = _uiState.value.copy(state = TypewriterState.IDLE)

        viewModelScope.launch {
            delay(SKIP_APPEAR_MS)
            if (_uiState.value.state != TypewriterState.NAVIGATING &&
                _uiState.value.state != TypewriterState.READY
            ) {
                _uiState.value = _uiState.value.copy(skipVisible = true)
            }
        }

        // T+200ms: cursor appears, typing begins
        delay(INITIAL_DELAY_MS)
        _uiState.value = _uiState.value.copy(
            state = TypewriterState.TYPING,
            cursorVisible = true
        )

        // Type each character at 40ms intervals
        val totalChars = QUOTE.length
        for (i in 1..totalChars) {
            if (_uiState.value.state == TypewriterState.NAVIGATING) return
            _uiState.value = _uiState.value.copy(visibleCharCount = i)
            delay(CHAR_DELAY_MS)
        }

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // CURSOR_FADE: stop blinking, hold visible 200ms, fade out 200ms
        _uiState.value = _uiState.value.copy(state = TypewriterState.CURSOR_FADE)

        delay(CURSOR_HOLD_MS)
        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        val fadeSteps = 10
        val fadeStepDuration = CURSOR_FADE_MS / fadeSteps
        for (step in 1..fadeSteps) {
            if (_uiState.value.state == TypewriterState.NAVIGATING) return
            val alpha = 1f - (step.toFloat() / fadeSteps)
            _uiState.value = _uiState.value.copy(cursorAlpha = alpha)
            delay(fadeStepDuration)
        }
        _uiState.value = _uiState.value.copy(cursorVisible = false, cursorAlpha = 0f)

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // PAUSE_1: 600ms silence
        _uiState.value = _uiState.value.copy(state = TypewriterState.PAUSE_1)
        delay(PAUSE_1_MS)

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // ATTRIBUTION: fade in 500ms, ease-out
        _uiState.value = _uiState.value.copy(state = TypewriterState.ATTRIBUTION)
        val attrSteps = 25
        val attrStepDuration = ATTRIBUTION_FADE_MS / attrSteps
        for (step in 1..attrSteps) {
            if (_uiState.value.state == TypewriterState.NAVIGATING) return
            val t = step.toFloat() / attrSteps
            val eased = 1f - (1f - t) * (1f - t)
            _uiState.value = _uiState.value.copy(attributionAlpha = eased)
            delay(attrStepDuration)
        }
        _uiState.value = _uiState.value.copy(attributionAlpha = 1f)

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // PAUSE_2: 800ms silence
        _uiState.value = _uiState.value.copy(state = TypewriterState.PAUSE_2)
        delay(PAUSE_2_MS)

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // CALL_TO_ACTION: slide up + fade in, 600ms ease-out
        _uiState.value = _uiState.value.copy(
            state = TypewriterState.CALL_TO_ACTION,
            skipVisible = true
        )
        val ctaSteps = 30
        val ctaStepDuration = CTA_SLIDE_MS / ctaSteps
        for (step in 1..ctaSteps) {
            if (_uiState.value.state == TypewriterState.NAVIGATING) return
            val t = step.toFloat() / ctaSteps
            val eased = 1f - (1f - t) * (1f - t)
            _uiState.value = _uiState.value.copy(
                ctaAlpha = eased,
                ctaTranslateY = 24f * (1f - eased)
            )
            delay(ctaStepDuration)
        }
        _uiState.value = _uiState.value.copy(ctaAlpha = 1f, ctaTranslateY = 0f)

        if (_uiState.value.state == TypewriterState.NAVIGATING) return

        // Fade in practice day number
        val pdSteps = 15
        val pdStepDuration = 300L / pdSteps
        for (step in 1..pdSteps) {
            if (_uiState.value.state == TypewriterState.NAVIGATING) return
            val t = step.toFloat() / pdSteps
            val eased = 1f - (1f - t) * (1f - t)
            _uiState.value = _uiState.value.copy(practiceDayAlpha = eased)
            delay(pdStepDuration)
        }
        _uiState.value = _uiState.value.copy(practiceDayAlpha = 1f)

        // READY: user can interact, chevron appears
        _uiState.value = _uiState.value.copy(state = TypewriterState.READY)
    }

    private suspend fun startReturnSequence() {
        _uiState.value = _uiState.value.copy(
            state = TypewriterState.READY,
            visibleCharCount = QUOTE.length,
            cursorVisible = false,
            cursorAlpha = 0f,
            attributionAlpha = 1f,
            ctaAlpha = 1f,
            ctaTranslateY = 0f,
            skipVisible = false,
            practiceDayAlpha = 1f
        )

        delay(RETURN_AUTO_ADVANCE_MS)
        if (!_uiState.value.isNavigating) {
            navigateAway()
        }
    }

    fun onSkip() {
        viewModelScope.launch {
            analyticsService.logTypewriterSkipped()
            _uiState.value = _uiState.value.copy(state = TypewriterState.NAVIGATING)
            navigateAway()
        }
    }

    fun onUserInteraction() {
        if (_uiState.value.state == TypewriterState.READY) {
            viewModelScope.launch {
                analyticsService.logTypewriterCompleted()
                navigateAway()
            }
        }
    }

    private suspend fun navigateAway() {
        if (_uiState.value.isNavigating) return
        _uiState.value = _uiState.value.copy(isNavigating = true)

        preferenceDao.insert(PreferenceEntity(PREF_KEY, "true"))

        val fadeSteps = 20
        val fadeStepDuration = SCREEN_FADE_MS / fadeSteps
        for (step in 1..fadeSteps) {
            val alpha = 1f - (step.toFloat() / fadeSteps)
            _uiState.value = _uiState.value.copy(screenAlpha = alpha)
            delay(fadeStepDuration)
        }
        _uiState.value = _uiState.value.copy(screenAlpha = 0f)
    }

    fun isNavigationComplete(): Boolean {
        return _uiState.value.isNavigating && _uiState.value.screenAlpha <= 0f
    }
}
