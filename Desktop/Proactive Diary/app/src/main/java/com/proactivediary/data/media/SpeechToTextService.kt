package com.proactivediary.data.media

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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Android's SpeechRecognizer for continuous speech-to-text.
 * Runs alongside AudioRecorderService — both share the mic.
 * Auto-restarts recognition for continuous dictation.
 */
@Singleton
class SpeechToTextService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isActive = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _finalText = MutableSharedFlow<String>(extraBufferCapacity = 20)
    val finalText: SharedFlow<String> = _finalText.asSharedFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    fun checkAvailability() {
        _isAvailable.value = SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!_isAvailable.value) return
        isActive = true
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
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
            speechRecognizer?.startListening(createIntent())
        } catch (_: Exception) {
            isActive = false
            _isListening.value = false
        }
    }

    private fun createIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
    }

    private fun restartIfActive() {
        if (isActive) {
            mainHandler.postDelayed({ createAndStart() }, 100)
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
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
                    restartIfActive()
                }
                SpeechRecognizer.ERROR_AUDIO -> {
                    // Mic conflict with MediaRecorder — stop gracefully
                    isActive = false
                    _isListening.value = false
                    _partialText.value = ""
                }
                SpeechRecognizer.ERROR_CLIENT -> {
                    // Client-side error, try to restart
                    restartIfActive()
                }
                else -> {
                    // Other errors — stop
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
