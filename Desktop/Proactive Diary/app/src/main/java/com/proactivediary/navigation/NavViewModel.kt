package com.proactivediary.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    init {
        viewModelScope.launch {
            // Initialize trial on first launch
            if (preferenceDao.get("trial_start_date") == null) {
                preferenceDao.insert(
                    PreferenceEntity("trial_start_date", System.currentTimeMillis().toString())
                )
            }

            val typewriterCompleted = preferenceDao.get("first_launch_completed")?.value == "true"

            // Set beautiful defaults for first-time users so Write tab looks great immediately
            if (!typewriterCompleted) {
                val defaults = listOf(
                    "diary_color" to "cream",
                    "diary_form" to "focused",
                    "diary_texture" to "paper",
                    "diary_canvas" to "lined",
                    "diary_details" to "[\"auto_save\",\"word_count\",\"date_header\",\"daily_quote\"]"
                )
                defaults.forEach { (key, value) ->
                    if (preferenceDao.get(key) == null) {
                        preferenceDao.insert(PreferenceEntity(key, value))
                    }
                }
            }

            // Always start at Typewriter — the Franklin quote is the first screen
            // in every user journey. First launch gets the full animation;
            // returning users see it instantly then auto-advance.
            _startDestination.value = Routes.Typewriter.route
        }
    }
}
