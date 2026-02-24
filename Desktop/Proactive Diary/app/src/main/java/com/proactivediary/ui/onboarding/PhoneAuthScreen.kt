package com.proactivediary.ui.onboarding

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.analytics.AnalyticsService

private val InstagramBlue = Color(0xFF3897F0)

@Composable
fun PhoneAuthScreen(
    onOtpSent: () -> Unit,
    onSignedIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    analyticsService: AnalyticsService,
    viewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var showCountryPicker by remember { mutableStateOf(false) }

    // Auto-detect country from SIM on first composition
    LaunchedEffect(Unit) {
        val detected = detectCountryFromSim(context)
        viewModel.selectCountry(detected)
        analyticsService.logOnboardingStart()
    }

    // Navigate when OTP sent
    LaunchedEffect(state.otpSent) {
        if (state.otpSent) onOtpSent()
    }

    // Navigate when signed in (auto-verify or Google)
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }

    if (showCountryPicker) {
        CountryPickerDialog(
            onDismiss = { showCountryPicker = false },
            onSelect = { country ->
                viewModel.selectCountry(country)
                showCountryPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .imePadding()
    ) {
        // Top bar with back arrow (hidden on first screen)
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(32.dp))

        if (state.isPhoneMode) {
            PhoneModeContent(
                state = state,
                onPhoneChange = viewModel::updatePhoneNumber,
                onChangeCountry = { showCountryPicker = true },
                onNext = { activity?.let { viewModel.sendOtp(it) } },
                onToggleMode = viewModel::toggleMode
            )
        } else {
            EmailModeContent(
                state = state,
                onEmailChange = viewModel::updateEmail,
                onNext = { viewModel.sendEmailAuth(context) },
                onToggleMode = viewModel::toggleMode,
                onGoogleSignIn = {
                    analyticsService.logOnboardingAuthStart()
                    viewModel.signInWithGoogle(context)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PhoneModeContent(
    state: PhoneAuthState,
    onPhoneChange: (String) -> Unit,
    onChangeCountry: () -> Unit,
    onNext: () -> Unit,
    onToggleMode: () -> Unit
) {
    // Title
    Text(
        text = "What's your mobile\nnumber?",
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Enter the mobile number where you can be contacted. No one will see this on your profile.",
        style = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Country code row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${state.countryFlag} ${state.countryName} (${state.countryCode})",
            style = TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Change",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InstagramBlue
            ),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onChangeCountry
            )
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Phone number input
    OutlinedTextField(
        value = state.phoneNumber,
        onValueChange = onPhoneChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Phone number",
                style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onNext() }),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Info text
    Text(
        text = "You may receive SMS notifications from us for security and login purposes.",
        style = TextStyle(
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            lineHeight = 16.sp
        )
    )

    // Error
    if (state.error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.error,
            style = TextStyle(fontSize = 13.sp, color = Color(0xFFEF4444))
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Next button
    NextButton(
        text = "Next",
        isLoading = state.isSending,
        onClick = onNext
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Toggle to email/Google mode
    ToggleModeButton(
        text = "Sign up with email or Google",
        onClick = onToggleMode
    )
}

@Composable
private fun EmailModeContent(
    state: PhoneAuthState,
    onEmailChange: (String) -> Unit,
    onNext: () -> Unit,
    onToggleMode: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Text(
        text = "Sign up with email\nor Google",
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 30.sp
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Enter your email address, or continue with Google.",
        style = TextStyle(
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = state.email,
        onValueChange = onEmailChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Email address",
                style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onNext() }),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
        )
    )

    if (state.error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.error,
            style = TextStyle(fontSize = 13.sp, color = Color(0xFFEF4444))
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    NextButton(
        text = "Next",
        isLoading = state.isSending,
        onClick = onNext
    )

    Spacer(modifier = Modifier.height(16.dp))

    // "or" divider
    OrDivider()

    Spacer(modifier = Modifier.height(16.dp))

    // Prominent Google sign-in button
    GoogleSignInButton(onClick = onGoogleSignIn)

    Spacer(modifier = Modifier.height(16.dp))

    ToggleModeButton(
        text = "Sign up with mobile number",
        onClick = onToggleMode
    )
}

@Composable
private fun NextButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(InstagramBlue)
            .clickable(enabled = !isLoading, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp).width(20.dp)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun ToggleModeButton(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        )
        Text(
            text = "or",
            style = TextStyle(
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        )
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoogleGIcon(modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continue with Google",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    val red = Color(0xFFEA4335)
    val yellow = Color(0xFFFBBC05)
    val green = Color(0xFF34A853)
    val blue = Color(0xFF4285F4)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.45f
        val strokeW = w * 0.18f

        // Draw colored arcs (the "G" shape)
        drawArc(
            color = red,
            startAngle = -30f,
            sweepAngle = -120f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        )
        drawArc(
            color = yellow,
            startAngle = 150f,
            sweepAngle = -60f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        )
        drawArc(
            color = green,
            startAngle = 90f,
            sweepAngle = -60f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        )
        drawArc(
            color = blue,
            startAngle = 30f,
            sweepAngle = -60f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW),
            topLeft = androidx.compose.ui.geometry.Offset(cx - r, cy - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        )
        // Blue horizontal bar (the crossbar of the G)
        drawRect(
            color = blue,
            topLeft = androidx.compose.ui.geometry.Offset(cx, cy - strokeW / 2f),
            size = androidx.compose.ui.geometry.Size(r + strokeW / 2f, strokeW)
        )
    }
}
