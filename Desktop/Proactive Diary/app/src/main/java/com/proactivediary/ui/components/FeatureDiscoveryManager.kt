package com.proactivediary.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages first-time feature discovery coach marks.
 * Tracks which tooltips have been shown via PreferenceDao.
 */
@HiltViewModel
class FeatureDiscoveryViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    companion object {
        const val KEY_WRITE_HINT = "coach_mark_write_shown"
        const val KEY_SWIPE_HINT = "coach_mark_swipe_shown"
        const val KEY_TAGS_HINT = "coach_mark_tags_shown"
        const val KEY_PHOTOS_HINT = "coach_mark_photos_shown"
    }

    private val _showWriteHint = MutableStateFlow(false)
    val showWriteHint: StateFlow<Boolean> = _showWriteHint.asStateFlow()

    private val _showSwipeHint = MutableStateFlow(false)
    val showSwipeHint: StateFlow<Boolean> = _showSwipeHint.asStateFlow()

    private val _showTagsHint = MutableStateFlow(false)
    val showTagsHint: StateFlow<Boolean> = _showTagsHint.asStateFlow()

    private val _showPhotosHint = MutableStateFlow(false)
    val showPhotosHint: StateFlow<Boolean> = _showPhotosHint.asStateFlow()

    init {
        checkHints()
    }

    private fun checkHints() {
        viewModelScope.launch {
            val writeShown = preferenceDao.get(KEY_WRITE_HINT)?.value == "true"
            val swipeShown = preferenceDao.get(KEY_SWIPE_HINT)?.value == "true"
            val tagsShown = preferenceDao.get(KEY_TAGS_HINT)?.value == "true"
            val photosShown = preferenceDao.get(KEY_PHOTOS_HINT)?.value == "true"
            val onboardingDone = preferenceDao.get("onboarding_completed")?.value == "true"
                    || preferenceDao.get("first_launch_completed")?.value == "true"

            // Only show hints after onboarding is complete
            if (onboardingDone) {
                // Single consolidated swipe hint (replaces old two-step write + swipe sequence)
                if (!swipeShown) _showSwipeHint.value = true
            }
        }
    }

    fun dismissWriteHint() {
        _showWriteHint.value = false
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity(KEY_WRITE_HINT, "true"))
            // Now check if swipe hint should show
            val swipeShown = preferenceDao.get(KEY_SWIPE_HINT)?.value == "true"
            if (!swipeShown) _showSwipeHint.value = true
        }
    }

    fun dismissSwipeHint() {
        _showSwipeHint.value = false
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity(KEY_SWIPE_HINT, "true"))
        }
    }

    fun dismissTagsHint() {
        _showTagsHint.value = false
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity(KEY_TAGS_HINT, "true"))
        }
    }

    fun dismissPhotosHint() {
        _showPhotosHint.value = false
        viewModelScope.launch {
            preferenceDao.insert(PreferenceEntity(KEY_PHOTOS_HINT, "true"))
        }
    }

    /** Called after first entry saved — trigger tags hint if not yet shown */
    fun onFirstEntrySaved() {
        viewModelScope.launch {
            val tagsShown = preferenceDao.get(KEY_TAGS_HINT)?.value == "true"
            if (!tagsShown) _showTagsHint.value = true
        }
    }
}
