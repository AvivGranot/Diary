package com.proactivediary.ui.onboarding

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import com.proactivediary.auth.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhoneAuthState(
    val phoneNumber: String = "",
    val countryCode: String = "+972",
    val countryName: String = "Israel",
    val countryFlag: String = "\uD83C\uDDEE\uD83C\uDDF1",
    val isPhoneMode: Boolean = true,
    val email: String = "",
    val isSending: Boolean = false,
    val otpSent: Boolean = false,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val otpCode: String = "",
    val isVerifying: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val resendCountdown: Int = 0
)

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow(PhoneAuthState(
        isSignedIn = authService.isAuthenticated
    ))
    val state: StateFlow<PhoneAuthState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun updatePhoneNumber(number: String) {
        _state.value = _state.value.copy(phoneNumber = number, error = null)
    }

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun selectCountry(country: Country) {
        _state.value = _state.value.copy(
            countryCode = country.code,
            countryName = country.name,
            countryFlag = country.flag,
            error = null
        )
    }

    fun toggleMode() {
        _state.value = _state.value.copy(
            isPhoneMode = !_state.value.isPhoneMode,
            error = null
        )
    }

    fun sendOtp(activity: Activity) {
        val s = _state.value
        val fullNumber = "${s.countryCode}${s.phoneNumber.trimStart('0')}"

        if (s.phoneNumber.length < 7) {
            _state.value = s.copy(error = "Please enter a valid phone number.")
            return
        }

        _state.value = s.copy(isSending = true, error = null)

        viewModelScope.launch {
            val result = authService.sendOtp(fullNumber, activity, s.resendToken)
            result.fold(
                onSuccess = { phoneAuthResult ->
                    if (phoneAuthResult.verificationId == "auto_verified") {
                        // Auto-verified — check auth state
                        _state.value = _state.value.copy(
                            isSending = false,
                            isSignedIn = authService.isAuthenticated
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isSending = false,
                            otpSent = true,
                            verificationId = phoneAuthResult.verificationId,
                            resendToken = phoneAuthResult.resendToken
                        )
                        startResendCountdown()
                    }
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = e.message ?: "Failed to send verification code."
                    )
                }
            )
        }
    }

    fun sendEmailAuth(activityContext: Context) {
        val s = _state.value
        val email = s.email.trim()

        if (email.isEmpty() || !email.contains("@")) {
            _state.value = s.copy(error = "Please enter a valid email address.")
            return
        }

        _state.value = s.copy(isSending = true, error = null)

        viewModelScope.launch {
            // Try sign in first, if fails try create
            val signInResult = authService.signInWithEmail(email, "proactive_temp_${email.hashCode()}")
            signInResult.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSending = false, isSignedIn = true)
                },
                onFailure = {
                    val createResult = authService.createAccountWithEmail(email, "proactive_temp_${email.hashCode()}")
                    createResult.fold(
                        onSuccess = {
                            _state.value = _state.value.copy(isSending = false, isSignedIn = true)
                        },
                        onFailure = { e ->
                            _state.value = _state.value.copy(
                                isSending = false,
                                error = e.message ?: "Email sign-in failed."
                            )
                        }
                    )
                }
            )
        }
    }

    fun updateOtpCode(code: String) {
        if (code.length <= 6) {
            _state.value = _state.value.copy(otpCode = code, error = null)
            if (code.length == 6) {
                verifyOtp()
            }
        }
    }

    fun verifyOtp() {
        val s = _state.value
        val verificationId = s.verificationId ?: return

        if (s.otpCode.length != 6) {
            _state.value = s.copy(error = "Please enter the 6-digit code.")
            return
        }

        _state.value = s.copy(isVerifying = true, error = null)

        viewModelScope.launch {
            val result = authService.verifyOtp(verificationId, s.otpCode)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isVerifying = false, isSignedIn = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isVerifying = false,
                        otpCode = "",
                        error = e.message ?: "Verification failed."
                    )
                }
            )
        }
    }

    fun resendOtp(activity: Activity) {
        if (_state.value.resendCountdown > 0) return
        sendOtp(activity)
    }

    fun signInWithGoogle(activityContext: Context) {
        _state.value = _state.value.copy(isSending = true, error = null)
        viewModelScope.launch {
            val result = authService.signInWithGoogle(activityContext)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSending = false, isSignedIn = true)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = e.message ?: "Google sign-in failed."
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun goBackToPhone() {
        _state.value = _state.value.copy(
            otpSent = false,
            otpCode = "",
            error = null
        )
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _state.value = _state.value.copy(resendCountdown = i)
                delay(1000L)
            }
        }
    }
}
