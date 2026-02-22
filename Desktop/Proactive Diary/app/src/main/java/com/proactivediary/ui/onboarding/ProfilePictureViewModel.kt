package com.proactivediary.ui.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.data.social.ProfileImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilePictureState(
    val selectedImageUri: Uri? = null,
    val googlePhotoUrl: String? = null,
    val isUploading: Boolean = false,
    val uploadedUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfilePictureViewModel @Inject constructor(
    private val profileImageRepository: ProfileImageRepository,
    private val auth: FirebaseAuth,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(ProfilePictureState())
    val state: StateFlow<ProfilePictureState> = _state.asStateFlow()

    init {
        // Pre-populate Google photo if available, with optimized size
        val googlePhoto = auth.currentUser?.photoUrl?.toString()
        if (!googlePhoto.isNullOrBlank()) {
            val optimizedUrl = if (googlePhoto.contains("googleusercontent.com")) {
                googlePhoto.replace(Regex("=s\\d+(-c)?"), "") + "=s256"
            } else googlePhoto
            _state.value = _state.value.copy(googlePhotoUrl = optimizedUrl)
        }
    }

    fun onImageSelected(uri: Uri) {
        _state.value = _state.value.copy(
            selectedImageUri = uri,
            error = null
        )
    }

    fun uploadSelectedPhoto() {
        val uri = _state.value.selectedImageUri ?: return
        _state.value = _state.value.copy(isUploading = true, error = null)

        viewModelScope.launch {
            val result = profileImageRepository.uploadAvatar(uri)
            result.fold(
                onSuccess = { url ->
                    analyticsService.logProfilePhotoUploaded(source = "gallery")
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadedUrl = url
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isUploading = false,
                        error = e.message ?: "Upload failed"
                    )
                }
            )
        }
    }

    fun useGooglePhoto() {
        val url = _state.value.googlePhotoUrl ?: return
        // Navigate immediately — Firestore write happens in background.
        // ensureMinimalProfile already wrote photoUrl; this is just a confirmation write.
        _state.value = _state.value.copy(uploadedUrl = url)
        viewModelScope.launch {
            try {
                profileImageRepository.useGooglePhotoAsDefault(url)
                analyticsService.logProfilePhotoUploaded(source = "google")
            } catch (_: Exception) { }
        }
    }
}
