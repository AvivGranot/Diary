package com.proactivediary.ui.onboarding

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.analytics.AnalyticsService

@Composable
fun OtpVerificationScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit,
    analyticsService: AnalyticsService,
    viewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            analyticsService.logOnboardingAuthComplete("phone", 0L)
            onVerified()
        }
    }

    // Auto-focus the code input
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button
        IconButton(
            onClick = {
                viewModel.goBackToPhone()
                onBack()
            },
            modifier = Modifier.padding(start = 0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Enter the confirmation\ncode",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 30.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle with masked phone number
        val maskedNumber = maskPhoneNumber(state.countryCode, state.phoneNumber)
        Text(
            text = "We sent a 6-digit code to $maskedNumber",
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP input boxes
        OtpInputField(
            code = state.otpCode,
            onCodeChange = viewModel::updateOtpCode,
            isError = state.error != null,
            focusRequester = focusRequester
        )

        // Error
        if (state.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.error!!,
                style = TextStyle(fontSize = 13.sp, color = Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Loading
        if (state.isVerifying) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Resend code
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (state.resendCountdown > 0) {
                Text(
                    text = "Resend code in ${state.resendCountdown}s",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            } else {
                Text(
                    text = "Resend code",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { activity?.let { viewModel.resendOtp(it) } }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OtpInputField(
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
    focusRequester: FocusRequester
) {
    val borderColor = if (isError) {
        Color(0xFFEF4444)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    }
    val filledBorderColor = if (isError) {
        Color(0xFFEF4444)
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    // Hidden text field that captures input
    Box {
        BasicTextField(
            value = code,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }
                onCodeChange(filtered)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .focusRequester(focusRequester)
        )

        // Visual OTP boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (i in 0 until 6) {
                val digit = code.getOrNull(i)?.toString() ?: ""
                val isFilled = digit.isNotEmpty()

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .then(
                            if (isFilled) {
                                Modifier.background(Color.Transparent)
                            } else Modifier
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = digit,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    )
                    // Bottom indicator line
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(2.dp)
                            .background(if (isFilled) filledBorderColor else borderColor)
                    )
                }
            }
        }
    }
}

private fun maskPhoneNumber(countryCode: String, phoneNumber: String): String {
    if (phoneNumber.length < 4) return "$countryCode $phoneNumber"
    val visible = phoneNumber.takeLast(4)
    val masked = "X".repeat((phoneNumber.length - 4).coerceAtLeast(0))
    return "$countryCode $masked $visible"
}
