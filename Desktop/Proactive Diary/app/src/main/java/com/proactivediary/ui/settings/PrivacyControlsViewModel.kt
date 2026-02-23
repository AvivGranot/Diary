package com.proactivediary.ui.settings

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
class PrivacyControlsViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao
) : ViewModel() {

    private val _photosEnabled = MutableStateFlow(true)
    val photosEnabled: StateFlow<Boolean> = _photosEnabled

    private val _locationEnabled = MutableStateFlow(true)
    val locationEnabled: StateFlow<Boolean> = _locationEnabled

    private val _calendarEnabled = MutableStateFlow(false)
    val calendarEnabled: StateFlow<Boolean> = _calendarEnabled

    private val _fitnessEnabled = MutableStateFlow(false)
    val fitnessEnabled: StateFlow<Boolean> = _fitnessEnabled

    private val _musicEnabled = MutableStateFlow(false)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled

    private val _cloudSyncEnabled = MutableStateFlow(false)
    val cloudSyncEnabled: StateFlow<Boolean> = _cloudSyncEnabled

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _photosEnabled.value = preferenceDao.get("privacy_photos")?.value != "false"
            _locationEnabled.value = preferenceDao.get("privacy_location")?.value != "false"
            _calendarEnabled.value = preferenceDao.get("privacy_calendar")?.value == "true"
            _fitnessEnabled.value = preferenceDao.get("privacy_fitness")?.value == "true"
            _musicEnabled.value = preferenceDao.get("privacy_music")?.value == "true"
            _cloudSyncEnabled.value = preferenceDao.get("privacy_cloud_sync")?.value == "true"
        }
    }

    fun togglePhotos(enabled: Boolean) {
        _photosEnabled.value = enabled
        persistToggle("privacy_photos", enabled)
    }

    fun toggleLocation(enabled: Boolean) {
        _locationEnabled.value = enabled
        persistToggle("privacy_location", enabled)
    }

    fun toggleCalendar(enabled: Boolean) {
        _calendarEnabled.value = enabled
        persistToggle("privacy_calendar", enabled)
    }

    fun toggleFitness(enabled: Boolean) {
        _fitnessEnabled.value = enabled
        persistToggle("privacy_fitness", enabled)
    }

    fun toggleMusic(enabled: Boolean) {
        _musicEnabled.value = enabled
        persistToggle("privacy_music", enabled)
    }

    fun toggleCloudSync(enabled: Boolean) {
        _cloudSyncEnabled.value = enabled
        persistToggle("privacy_cloud_sync", enabled)
    }

    private fun persistToggle(key: String, enabled: Boolean) {
        viewModelScope.launch {
            preferenceDao.insert(
                PreferenceEntity(key, if (enabled) "true" else "false")
            )
        }
    }
}
