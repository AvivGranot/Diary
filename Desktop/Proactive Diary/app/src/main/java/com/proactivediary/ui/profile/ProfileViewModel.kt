package com.proactivediary.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.data.social.ProfileImageRepository
import com.proactivediary.data.social.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String = "",
    val handle: String = "",
    val photoUrl: String? = null,
    val isSignedIn: Boolean = false,
    val totalEntries: Int = 0,
    val totalLikesReceived: Int = 0,
    val totalQuotes: Int = 0,
    val totalWords: Int = 0,
    val entriesThisYear: Int = 0,
    val bestWritingDay: String = "",
    val bestWritingDayPercent: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val quotesRepository: QuotesRepository,
    private val profileImageRepository: ProfileImageRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadEntryStats()
        loadSocialStats()
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        val name = user?.displayName ?: "Writer"
        val email = user?.email ?: ""
        val handle = if (email.contains("@")) "@${email.substringBefore("@")}" else ""
        val photoUrl = user?.photoUrl?.toString()

        _uiState.value = _uiState.value.copy(
            displayName = name,
            handle = handle,
            photoUrl = photoUrl,
            isSignedIn = user != null
        )

        // Also try to get photoUrl from Firestore (may be updated via profile picture upload)
        val uid = user?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val firestorePhoto = doc.getString("photoUrl")
                if (!firestorePhoto.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(photoUrl = firestorePhoto)
                }
            } catch (_: Exception) {
                // Non-fatal: fall back to Firebase Auth photo
            }
        }
    }

    private fun loadEntryStats() {
        viewModelScope.launch {
            try {
                val allEntries = withContext(Dispatchers.IO) {
                    entryRepository.getAllSync()
                }

                val zone = ZoneId.systemDefault()
                val currentYear = java.time.LocalDate.now().year

                // Total entries
                val totalEntries = allEntries.size

                // Total words
                val totalWords = allEntries.sumOf { it.wordCount }

                // Entries this year
                val entriesThisYear = allEntries.count { entry ->
                    val entryYear = Instant.ofEpochMilli(entry.createdAt)
                        .atZone(zone)
                        .toLocalDate()
                        .year
                    entryYear == currentYear
                }

                // Best writing day
                val dayOfWeekCounts = allEntries.map { entry ->
                    Instant.ofEpochMilli(entry.createdAt)
                        .atZone(zone)
                        .toLocalDate()
                        .dayOfWeek
                }.groupingBy { it }.eachCount()

                val bestDay = dayOfWeekCounts.maxByOrNull { it.value }
                val bestDayName = bestDay?.key?.getDisplayName(
                    JavaTextStyle.FULL, Locale.getDefault()
                ) ?: ""
                val bestDayPercent = if (allEntries.isNotEmpty() && bestDay != null) {
                    (bestDay.value * 100) / allEntries.size
                } else 0

                _uiState.value = _uiState.value.copy(
                    totalEntries = totalEntries,
                    totalWords = totalWords,
                    entriesThisYear = entriesThisYear,
                    bestWritingDay = bestDayName,
                    bestWritingDayPercent = bestDayPercent,
                    isLoading = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val result = profileImageRepository.uploadAvatar(uri)
            result.onSuccess { url ->
                _uiState.value = _uiState.value.copy(photoUrl = url)
            }
        }
    }

    private fun loadSocialStats() {
        val uid = auth.currentUser?.uid ?: return

        // Load my quotes and aggregate likes
        viewModelScope.launch {
            try {
                quotesRepository.observeMyQuotes().collect { myQuotes ->
                    val totalLikes = myQuotes.sumOf { it.likeCount }
                    _uiState.value = _uiState.value.copy(
                        totalQuotes = myQuotes.size,
                        totalLikesReceived = totalLikes
                    )
                }
            } catch (_: Exception) {
                // Non-fatal
            }
        }
    }
}
