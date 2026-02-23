package com.proactivediary.ui.story

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.ai.GrokImageResult
import com.proactivediary.data.ai.GrokImageService
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.domain.model.StoryAspectRatio
import com.proactivediary.domain.model.StoryStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class StoryGeneratorUiState(
    val entryId: String = "",
    val title: String = "",
    val excerpt: String = "",
    val dateHeader: String = "",
    val location: String? = null,
    val weather: String? = null,
    val selectedStyle: StoryStyle = StoryStyle.DREAMY,
    val selectedRatio: StoryAspectRatio = StoryAspectRatio.STORY,
    val overlayText: Boolean = false,
    val overlayQuote: String = "",
    val isGenerating: Boolean = false,
    val generatedImageFile: File? = null,
    val errorMessage: String? = null,
    val generationCount: Int = 0,
    val isApiAvailable: Boolean = true,
    val isLoaded: Boolean = false
)

@HiltViewModel
class StoryGeneratorViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val grokImageService: GrokImageService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryGeneratorUiState())
    val uiState: StateFlow<StoryGeneratorUiState> = _uiState.asStateFlow()

    private val entryId: String = savedStateHandle.get<String>("entryId") ?: ""

    init {
        _uiState.value = _uiState.value.copy(isApiAvailable = grokImageService.isApiAvailable)
        loadEntry()
    }

    private fun loadEntry() {
        if (entryId.isBlank()) return
        viewModelScope.launch {
            val entry = entryRepository.getByIdSync(entryId) ?: return@launch

            val plainText = entry.contentPlain ?: entry.content
            val excerpt = if (plainText.length > 150) plainText.take(150).trimEnd() + "..." else plainText

            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
            val dateHeader = Instant.ofEpochMilli(entry.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)

            val weather = if (entry.weatherCondition != null && entry.weatherTemp != null) {
                "${entry.weatherCondition}, ${entry.weatherTemp.toInt()}°"
            } else null

            _uiState.value = _uiState.value.copy(
                entryId = entry.id,
                title = entry.title,
                excerpt = excerpt,
                dateHeader = dateHeader,
                location = entry.locationName,
                weather = weather,
                overlayQuote = entry.title.ifBlank { plainText.take(80).trimEnd() },
                isLoaded = true
            )
        }
    }

    fun selectStyle(style: StoryStyle) {
        _uiState.value = _uiState.value.copy(selectedStyle = style)
    }

    fun selectRatio(ratio: StoryAspectRatio) {
        _uiState.value = _uiState.value.copy(selectedRatio = ratio)
    }

    fun toggleOverlay(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(overlayText = enabled)
    }

    fun updateOverlayQuote(text: String) {
        _uiState.value = _uiState.value.copy(overlayQuote = text)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun generate() {
        val state = _uiState.value
        if (state.isGenerating) return

        _uiState.value = state.copy(isGenerating = true, errorMessage = null)

        viewModelScope.launch {
            val entry = entryRepository.getByIdSync(entryId)
            if (entry == null) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Entry not found"
                )
                return@launch
            }

            when (val result = grokImageService.generateImage(entry, state.selectedStyle, state.selectedRatio)) {
                is GrokImageResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        generatedImageFile = result.imageFile,
                        generationCount = _uiState.value.generationCount + 1
                    )
                }
                is GrokImageResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun regenerate() {
        _uiState.value = _uiState.value.copy(generatedImageFile = null)
        generate()
    }

    fun getFinalImageFile(context: Context): File? {
        val state = _uiState.value
        val sourceFile = state.generatedImageFile ?: return null

        if (!state.overlayText || state.overlayQuote.isBlank()) {
            return sourceFile
        }

        return try {
            val original = BitmapFactory.decodeFile(sourceFile.absolutePath)
                ?: return sourceFile
            val bitmap = original.copy(Bitmap.Config.ARGB_8888, true)
            original.recycle()
            val canvas = Canvas(bitmap)
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()

            // Bottom gradient overlay (bottom 35%)
            val gradientTop = height * 0.65f
            val gradientPaint = Paint().apply {
                shader = LinearGradient(
                    0f, gradientTop, 0f, height,
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb(180, 0, 0, 0),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, gradientTop, width, height, gradientPaint)

            // Quote text
            val textSize = width * 0.045f
            val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                this.textSize = textSize
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            val margin = width * 0.06f
            val quoteText = "\u201C${state.overlayQuote}\u201D"

            // Simple line-wrap
            val maxWidth = width - margin * 2
            val lines = wrapText(quoteText, quotePaint, maxWidth)
            var y = height - margin - (lines.size * textSize * 1.3f) - textSize * 2
            for (line in lines) {
                canvas.drawText(line, margin, y, quotePaint)
                y += textSize * 1.3f
            }

            // Date line
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(180, 255, 255, 255)
                this.textSize = textSize * 0.65f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            canvas.drawText(state.dateHeader, margin, height - margin - textSize, datePaint)

            // Watermark
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(90, 255, 255, 255)
                this.textSize = textSize * 0.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                letterSpacing = 0.15f
            }
            val wmText = "PROACTIVE DIARY"
            val wmWidth = wmPaint.measureText(wmText)
            canvas.drawText(wmText, width - margin - wmWidth, height - margin, wmPaint)

            // Save to cache
            val outputFile = File(sourceFile.parentFile, "story_overlay_${System.currentTimeMillis()}.png")
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            outputFile
        } catch (_: OutOfMemoryError) {
            sourceFile
        } catch (_: Exception) {
            sourceFile
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
