package com.proactivediary.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proactivediary.auth.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickAuthState(
    val isSigningIn: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QuickAuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAuthState(
        isSignedIn = authService.isAuthenticated
    ))
    val state: StateFlow<QuickAuthState> = _state.asStateFlow()

    fun signInWithGoogle(activityContext: Context) {
        _state.value = _state.value.copy(isSigningIn = true, error = null)

        viewModelScope.launch {
            val result = authService.signInWithGoogle(activityContext)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSigningIn = false, isSignedIn = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSigningIn = false,
                        error = e.message ?: "Sign-in failed. Please try again."
                    )
                }
            )
        }
    }
}
