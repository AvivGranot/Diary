package com.proactivediary.ui.designstudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.domain.model.DiaryThemeConfig
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.diaryColorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DesignStudioState(
    val selectedColor: String = "cream",
    val selectedForm: String = "focused",
    val selectedTexture: String = "paper",
    val selectedCanvas: String = "lined",
    val features: Map<String, Boolean> = mapOf(
        "auto_save" to true,
        "word_count" to true,
        "mood_prompt" to false,
        "daily_quote" to true,
        "date_header" to true
    ),
    val markText: String = "",
    val markPosition: String = "header",
    val markFont: String = "serif",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
) {
    val colorDisplayName: String
        get() = diaryColorOptions.find { it.key == selectedColor }?.name ?: "Cream"

    val formDisplayName: String
        get() = selectedForm.replaceFirstChar { it.uppercase() }

    val textureDisplayName: String
        get() = selectedTexture.replaceFirstChar { it.uppercase() }

    val canvasDisplayName: String
        get() = selectedCanvas.replaceFirstChar { it.uppercase() }

    val configLabel: String
        get() = "${colorDisplayName.uppercase()} \u00B7 ${canvasDisplayName.uppercase()} \u00B7 ${textureDisplayName.uppercase()}"

    val summaryLabel: String
        get() = "${colorDisplayName.uppercase()} \u00B7 ${formDisplayName.uppercase()} \u00B7 ${textureDisplayName.uppercase()}"

    val enabledFeaturesList: List<String>
        get() = features.filter { it.value }.keys.toList()

    val themeConfig: DiaryThemeConfig
        get() = DiaryThemeConfig(
            colorScheme = selectedColor,
            primaryColor = DiaryThemeConfig.colorForKey(selectedColor),
            form = selectedForm,
            texture = selectedTexture,
            canvas = selectedCanvas,
            features = enabledFeaturesList,
            markText = markText,
            markPosition = markPosition,
            markFont = markFont
        )
}

@HiltViewModel
class DesignStudioViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _state = MutableStateFlow(DesignStudioState())
    val state: StateFlow<DesignStudioState> = _state.asStateFlow()

    private val gson = Gson()

    init {
        loadSavedPreferences()
    }

    private fun loadSavedPreferences() {
        viewModelScope.launch {
            try {
                val color = preferenceDao.get("diary_color")?.value
                val form = preferenceDao.get("diary_form")?.value
                val texture = preferenceDao.get("diary_texture")?.value
                val canvas = preferenceDao.get("diary_canvas")?.value
                val detailsJson = preferenceDao.get("diary_details")?.value
                val markText = preferenceDao.get("diary_mark_text")?.value
                val markPosition = preferenceDao.get("diary_mark_position")?.value
                val markFont = preferenceDao.get("diary_mark_font")?.value

                val features = if (detailsJson != null) {
                    try {
                        val enabledList = gson.fromJson(detailsJson, Array<String>::class.java).toList()
                        mapOf(
                            "auto_save" to ("auto_save" in enabledList),
                            "word_count" to ("word_count" in enabledList),
                            "mood_prompt" to ("mood_prompt" in enabledList),
                            "daily_quote" to ("daily_quote" in enabledList),
                            "date_header" to ("date_header" in enabledList)
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else null

                _state.update { current ->
                    current.copy(
                        selectedColor = color ?: current.selectedColor,
                        selectedForm = form ?: current.selectedForm,
                        selectedTexture = texture ?: current.selectedTexture,
                        selectedCanvas = canvas ?: current.selectedCanvas,
                        features = features ?: current.features,
                        markText = markText ?: current.markText,
                        markPosition = markPosition ?: current.markPosition,
                        markFont = markFont ?: current.markFont,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectColor(key: String) {
        _state.update { it.copy(selectedColor = key) }
    }

    fun selectForm(form: String) {
        _state.update { it.copy(selectedForm = form) }
    }

    fun selectTexture(texture: String) {
        _state.update { it.copy(selectedTexture = texture) }
    }

    fun selectCanvas(canvas: String) {
        _state.update { it.copy(selectedCanvas = canvas) }
    }

    fun toggleFeature(feature: String) {
        _state.update { current ->
            val updated = current.features.toMutableMap()
            updated[feature] = !(updated[feature] ?: false)
            current.copy(features = updated)
        }
    }

    fun updateMarkText(text: String) {
        if (text.length <= 20) {
            _state.update { it.copy(markText = text) }
        }
    }

    fun selectMarkPosition(position: String) {
        _state.update { it.copy(markPosition = position) }
    }

    fun selectMarkFont(font: String) {
        _state.update { it.copy(markFont = font) }
    }

    fun saveAndNavigate(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            try {
                val current = _state.value

                preferenceDao.insert(PreferenceEntity("diary_color", current.selectedColor))
                preferenceDao.insert(PreferenceEntity("diary_form", current.selectedForm))
                preferenceDao.insert(PreferenceEntity("diary_texture", current.selectedTexture))
                preferenceDao.insert(PreferenceEntity("diary_canvas", current.selectedCanvas))

                val detailsJson = gson.toJson(current.enabledFeaturesList)
                preferenceDao.insert(PreferenceEntity("diary_details", detailsJson))

                preferenceDao.insert(PreferenceEntity("diary_mark_text", current.markText))
                preferenceDao.insert(PreferenceEntity("diary_mark_position", current.markPosition))
                preferenceDao.insert(PreferenceEntity("diary_mark_font", current.markFont))

                preferenceDao.insert(PreferenceEntity("design_completed", "true"))

                onComplete()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
