package com.proactivediary.data.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android's SpeechRecognizer for continuous speech-to-text.
 * Tries Google's recognizer first (most reliable for partial results),
 * then falls back to the device default.
 * Auto-restarts recognition for continuous dictation.
 */
@Singleton
class SpeechToTextService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isActive = false
    private var consecutiveErrors = 0
    private var useGoogleRecognizer = true
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _finalText = MutableSharedFlow<String>(extraBufferCapacity = 20)
    val finalText: SharedFlow<String> = _finalText.asSharedFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val error: SharedFlow<String> = _error.asSharedFlow()

    companion object {
        private val GOOGLE_RECOGNIZER = ComponentName(
            "com.google.android.googlequicksearchbox",
            "com.google.android.voicesearch.serviceapi.GoogleRecognitionService"
        )
    }

    fun checkAvailability() {
        _isAvailable.value = SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!_isAvailable.value) {
            _error.tryEmit("Speech recognition not available on this device")
            return
        }
        isActive = true
        consecutiveErrors = 0
        useGoogleRecognizer = true
        _isListening.value = true
        _partialText.value = ""
        mainHandler.post { createAndStart() }
    }

    fun stopListening() {
        isActive = false
        _isListening.value = false
        _partialText.value = ""
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
        }
    }

    private fun createAndStart() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null

            // Try Google's recognizer first (best partial results support),
            // fall back to device default if Google isn't available
            speechRecognizer = if (useGoogleRecognizer) {
                try {
                    SpeechRecognizer.createSpeechRecognizer(context, GOOGLE_RECOGNIZER)
                } catch (_: Exception) {
                    useGoogleRecognizer = false
                    SpeechRecognizer.createSpeechRecognizer(context)
                }
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(listener)
            speechRecognizer?.startListening(createIntent())
        } catch (e: Exception) {
            if (useGoogleRecognizer) {
                // Google recognizer failed — retry with device default
                useGoogleRecognizer = false
                mainHandler.postDelayed({ createAndStart() }, 100)
            } else {
                isActive = false
                _isListening.value = false
                _error.tryEmit("Could not start speech recognition. Check that Google app is installed.")
            }
        }
    }

    private fun createIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
    }

    private fun restartIfActive() {
        if (isActive && consecutiveErrors < 5) {
            val delay = if (consecutiveErrors > 0) 200L * consecutiveErrors else 50L
            mainHandler.postDelayed({ createAndStart() }, delay)
        } else if (consecutiveErrors >= 5) {
            isActive = false
            _isListening.value = false
            if (useGoogleRecognizer) {
                // Google recognizer kept failing — try device default
                useGoogleRecognizer = false
                consecutiveErrors = 0
                isActive = true
                _isListening.value = true
                mainHandler.postDelayed({ createAndStart() }, 200)
            } else {
                _error.tryEmit("Dictation stopped — too many errors. Tap to retry.")
            }
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Recognition is ready — reset error counter
            consecutiveErrors = 0
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            // Will get results callback, then restart
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    // Normal: silence timeout, restart for continuous listening
                    _partialText.value = ""
                    consecutiveErrors = 0
                    restartIfActive()
                }
                SpeechRecognizer.ERROR_AUDIO -> {
                    isActive = false
                    _isListening.value = false
                    _partialText.value = ""
                    _error.tryEmit("Microphone is busy. Close other apps using the mic and try again.")
                }
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                    // Network-based recognizer can't reach server
                    consecutiveErrors++
                    _partialText.value = ""
                    if (consecutiveErrors >= 3 && useGoogleRecognizer) {
                        // Switch to default recognizer (might be offline-capable)
                        useGoogleRecognizer = false
                        consecutiveErrors = 0
                        restartIfActive()
                    } else if (consecutiveErrors >= 5) {
                        isActive = false
                        _isListening.value = false
                        _error.tryEmit("No internet connection. Speech recognition needs network access.")
                    } else {
                        restartIfActive()
                    }
                }
                SpeechRecognizer.ERROR_CLIENT -> {
                    consecutiveErrors++
                    if (consecutiveErrors == 1 && useGoogleRecognizer) {
                        // Google recognizer may not work on this device — switch immediately
                        useGoogleRecognizer = false
                        consecutiveErrors = 0
                    }
                    restartIfActive()
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    isActive = false
                    _isListening.value = false
                    _error.tryEmit("Microphone permission is required for dictation.")
                }
                else -> {
                    consecutiveErrors++
                    _partialText.value = ""
                    restartIfActive()
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?.trim()
            if (!text.isNullOrBlank()) {
                _finalText.tryEmit(text)
            }
            _partialText.value = ""
            consecutiveErrors = 0
            restartIfActive()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?.trim()
            if (!text.isNullOrBlank()) {
                _partialText.value = text
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
