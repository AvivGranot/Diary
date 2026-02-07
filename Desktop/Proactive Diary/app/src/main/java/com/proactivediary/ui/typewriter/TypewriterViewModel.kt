package com.proactivediary.ui.typewriter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
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
    val skipVisible: Boolean = false,
    val screenAlpha: Float = 1f,
    val isFirstLaunch: Boolean = true,
    val isNavigating: Boolean = false,
    val isLoaded: Boolean = false
)

@HiltViewModel
class TypewriterViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    companion object {
        const val QUOTE = "If you would not be forgotten,\n" +
                "as soon as you are dead and rotten,\n" +
                "either write things worth reading,\n" +
                "or do things worth writing"
        const val ATTRIBUTION = "Benjamin Franklin"
        const val CTA = "Now write yours."

        private const val CHAR_DELAY_MS = 40L
        private const val INITIAL_DELAY_MS = 200L
        private const val CURSOR_HOLD_MS = 200L
        private const val CURSOR_FADE_MS = 200L
        private const val PAUSE_1_MS = 600L
        private const val ATTRIBUTION_FADE_MS = 500L
        private const val PAUSE_2_MS = 800L
        private const val CTA_SLIDE_MS = 600L
        private const val SKIP_APPEAR_MS = 1500L
        private const val SCREEN_FADE_MS = 400L
        private const val RETURN_AUTO_ADVANCE_MS = 1000L

        private const val PREF_KEY = "first_launch_completed"
    }

    private val _uiState = MutableStateFlow(TypewriterUiState())
    val uiState: StateFlow<TypewriterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            checkFirstLaunch()
        }
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
            skipVisible = false
        )

        delay(RETURN_AUTO_ADVANCE_MS)
        if (!_uiState.value.isNavigating) {
            navigateAway()
        }
    }

    fun onSkip() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(state = TypewriterState.NAVIGATING)
            navigateAway()
        }
    }

    fun onUserInteraction() {
        if (_uiState.value.state == TypewriterState.READY) {
            viewModelScope.launch {
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
