package com.proactivediary.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.ui.theme.accentColorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LayoutUiState(
    val themeMode: String = "dark",
    val accentIndex: Int = 0,
    val accentKey: String = "mint",
    val fontSizeIndex: Int = 1 // 0=small, 1=medium, 2=large
)

@HiltViewModel
class LayoutViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    var uiState by mutableStateOf(LayoutUiState())
        private set

    init {
        viewModelScope.launch {
            val themeMode = preferenceDao.get("theme_mode")?.value ?: "dark"
            val accentKey = preferenceDao.get("accent_color")?.value ?: "mint"
            val fontSizePref = preferenceDao.get("font_size")?.value ?: "medium"

            val accentIndex = accentColorOptions.indexOfFirst { it.key == accentKey }
                .takeIf { it >= 0 } ?: 0

            val fontSizeIndex = when (fontSizePref) {
                "small" -> 0; "large" -> 2; else -> 1
            }

            uiState = LayoutUiState(
                themeMode = themeMode,
                accentIndex = accentIndex,
                accentKey = accentKey,
                fontSizeIndex = fontSizeIndex
            )
        }
    }

    fun setThemeMode(mode: String) {
        uiState = uiState.copy(themeMode = mode)
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("theme_mode", mode))
        }
    }

    fun setAccentColor(index: Int, key: String) {
        uiState = uiState.copy(accentIndex = index, accentKey = key)
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("accent_color", key))
        }
    }

    fun setFontSize(index: Int) {
        uiState = uiState.copy(fontSizeIndex = index)
        val key = when (index) { 0 -> "small"; 2 -> "large"; else -> "medium" }
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity("font_size", key))
        }
    }
}
