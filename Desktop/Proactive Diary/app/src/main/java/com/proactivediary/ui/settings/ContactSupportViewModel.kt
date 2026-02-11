package com.proactivediary.ui.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.proactivediary.BuildConfig
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DeviceInfo(
    val appVersion: String = "",
    val androidVersion: String = "",
    val deviceModel: String = "",
    val planType: String = "",
    val totalEntries: Int = 0
)

data class ContactSupportUiState(
    val category: String = "support",
    val email: String = "",
    val subject: String = "",
    val description: String = "",
    val emailError: String? = null,
    val subjectError: String? = null,
    val descriptionError: String? = null,
    val isSubmitting: Boolean = false,
    val submitResult: String? = null,
    val deviceInfo: DeviceInfo = DeviceInfo()
)

@HiltViewModel
class ContactSupportViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val preferenceDao: PreferenceDao,
    private val entryRepository: EntryRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactSupportUiState(
        category = savedStateHandle.get<String>("category") ?: "support"
    ))
    val uiState: StateFlow<ContactSupportUiState> = _uiState.asStateFlow()

    init {
        collectDeviceInfo()
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun setSubject(subject: String) {
        _uiState.update { it.copy(subject = subject, subjectError = null) }
    }

    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun clearResult() {
        _uiState.update { it.copy(submitResult = null) }
    }

    fun submit() {
        val state = _uiState.value

        // Validate
        var hasError = false
        if (state.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email") }
            hasError = true
        }
        if (state.subject.isBlank()) {
            _uiState.update { it.copy(subjectError = "Subject is required") }
            hasError = true
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                // Try Cloud Function first
                val data = hashMapOf(
                    "category" to state.category,
                    "email" to state.email,
                    "subject" to state.subject,
                    "description" to state.description,
                    "appVersion" to state.deviceInfo.appVersion,
                    "androidVersion" to state.deviceInfo.androidVersion,
                    "deviceModel" to state.deviceInfo.deviceModel,
                    "planType" to state.deviceInfo.planType,
                    "totalEntries" to state.deviceInfo.totalEntries
                )
                Firebase.functions
                    .getHttpsCallable("submitSupportRequest")
                    .call(data)
                    .await()

                analyticsService.logSupportRequestSubmitted(state.category)
                _uiState.update { it.copy(isSubmitting = false, submitResult = "sent") }
            } catch (e: Exception) {
                // Fallback to email intent
                analyticsService.logSupportRequestSubmitted("${state.category}_email_fallback")
                _uiState.update { it.copy(isSubmitting = false, submitResult = "fallback") }
            }
        }
    }

    private fun collectDeviceInfo() {
        viewModelScope.launch {
            val (planType, totalEntries) = withContext(Dispatchers.IO) {
                val plan = preferenceDao.getSync("billing_cached_plan")?.value ?: "trial"
                val entries = entryRepository.getTotalEntryCount()
                Pair(plan, entries)
            }
            _uiState.update {
                it.copy(
                    deviceInfo = DeviceInfo(
                        appVersion = BuildConfig.VERSION_NAME,
                        androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                        planType = planType,
                        totalEntries = totalEntries
                    )
                )
            }
        }
    }
}
