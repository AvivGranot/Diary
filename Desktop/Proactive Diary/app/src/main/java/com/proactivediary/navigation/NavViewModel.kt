package com.proactivediary.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.analytics.Experiment
import com.proactivediary.analytics.ExperimentService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val experimentService: ExperimentService
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

            _startDestination.value = if (typewriterCompleted) {
                Routes.Main.route
            } else {
                // Experiment 1: First Words — vary the onboarding flow
                val variant = experimentService.getVariant(Experiment.FIRST_WORDS)
                experimentService.logExposure(Experiment.FIRST_WORDS)
                when (variant) {
                    "straight_to_write" -> {
                        // Skip everything — mark onboarding as done and go to Main
                        preferenceDao.insert(PreferenceEntity("first_launch_completed", "true"))
                        Routes.Main.route
                    }
                    "skip_design" -> {
                        // Show typewriter but skip Design Studio (nav goes straight to Main after)
                        preferenceDao.insert(PreferenceEntity("exp_skip_design", "true"))
                        Routes.Typewriter.route
                    }
                    else -> Routes.Typewriter.route // control: full flow
                }
            }
        }
    }
}
