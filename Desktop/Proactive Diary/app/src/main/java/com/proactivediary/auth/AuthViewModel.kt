package com.proactivediary.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val showEmailForm: Boolean = false,
    val isCreateAccount: Boolean = true
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(
        isSignedIn = authService.isAuthenticated,
        userEmail = authService.userEmail,
        userDisplayName = authService.userDisplayName
    ))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isAuthenticated: Boolean
        get() = authService.isAuthenticated

    fun signInWithGoogle(activityContext: Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authService.signInWithGoogle(activityContext)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userEmail = user.email,
                        userDisplayName = user.displayName,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sign-in failed"
                    )
                }
            )
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = _uiState.value.copy(
                error = if (email.isBlank()) "Enter your email" else "Password must be at least 6 characters"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authService.signInWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userEmail = user.email,
                        userDisplayName = user.displayName,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sign-in failed"
                    )
                }
            )
        }
    }

    fun createAccountWithEmail(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = _uiState.value.copy(
                error = if (email.isBlank()) "Enter your email" else "Password must be at least 6 characters"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authService.createAccountWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedIn = true,
                        userEmail = user.email,
                        userDisplayName = user.displayName,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Account creation failed"
                    )
                }
            )
        }
    }

    fun toggleEmailForm() {
        _uiState.value = _uiState.value.copy(
            showEmailForm = !_uiState.value.showEmailForm,
            error = null
        )
    }

    fun toggleCreateAccount() {
        _uiState.value = _uiState.value.copy(
            isCreateAccount = !_uiState.value.isCreateAccount,
            error = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun signOut() {
        authService.signOut()
        _uiState.value = AuthUiState()
    }
}
