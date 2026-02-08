package com.proactivediary.ui.typewriter

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond
import com.proactivediary.ui.theme.DiaryColors

@Composable
fun TypewriterScreen(
    onNavigateForward: () -> Unit,
    viewModel: TypewriterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    // Immersive mode: hide system bars
    DisposableEffect(Unit) {
        val activity = view.context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Navigate when fade-out completes
    LaunchedEffect(uiState.screenAlpha, uiState.isNavigating) {
        if (viewModel.isNavigationComplete()) {
            onNavigateForward()
        }
    }

    // Suppress back button
    BackHandler { }

    if (!uiState.isLoaded) return

    // Cursor blink: step function, 530ms — only active during TYPING state
    val cursorBlinkOn by rememberCursorBlinkState(
        intervalMs = 530L,
        enabled = uiState.cursorVisible && uiState.state == TypewriterState.TYPING
    )

    // Chevron pulse: 0.4→1.0→0.4, 2000ms period, ease-in-out
    val infiniteTransition = rememberInfiniteTransition(label = "chevron")
    val chevronAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chevronAlpha"
    )

    val pencilColor = Color(0xFF585858)
    val inkColor = Color(0xFF313131)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper)
            .graphicsLayer { alpha = uiState.screenAlpha }
            .pointerInput(uiState.state) {
                if (uiState.state == TypewriterState.READY) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -10f) {
                            viewModel.onUserInteraction()
                        }
                    }
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (uiState.state == TypewriterState.READY) {
                    viewModel.onUserInteraction()
                }
            }
    ) {
        if (uiState.showWelcomeBack) {
            // Welcome back screen for lapsed users (7+ days inactive)
            WelcomeBackContent(
                data = uiState.welcomeBackData,
                alpha = uiState.welcomeBackAlpha,
                chevronAlpha = chevronAlpha,
                inkColor = inkColor,
                pencilColor = pencilColor
            )
        } else {
            // Skip button — top right, 300ms fade-in ease-out
            AnimatedVisibility(
                visible = uiState.skipVisible && !uiState.isNavigating,
                enter = fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(0)),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = "Skip",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = pencilColor
                    ),
                    modifier = Modifier
                        .padding(16.dp)
                        .size(48.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.onSkip()
                        }
                        .padding(top = 14.dp)
                )
            }

            // Main content — top-aligned, centered horizontally
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 80.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Typewriter quote canvas
                TypewriterCanvas(
                    quote = TypewriterViewModel.QUOTE,
                    visibleCharCount = uiState.visibleCharCount,
                    cursorVisible = uiState.cursorVisible,
                    cursorAlpha = uiState.cursorAlpha,
                    cursorBlinkOn = cursorBlinkOn
                )

                // Attribution: "Benjamin Franklin"
                if (uiState.attributionAlpha > 0f) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = TypewriterViewModel.ATTRIBUTION,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            letterSpacing = 0.7.sp,
                            color = pencilColor
                        ),
                        modifier = Modifier.alpha(uiState.attributionAlpha)
                    )
                }

                // CTA: "Now write yours."
                if (uiState.ctaAlpha > 0f) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = TypewriterViewModel.CTA,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Normal,
                            color = inkColor
                        ),
                        modifier = Modifier
                            .alpha(uiState.ctaAlpha)
                            .graphicsLayer {
                                translationY = uiState.ctaTranslateY * density
                            }
                    )
                }

                // Downward chevron — pulsing, appears with READY state
                if (uiState.ctaAlpha > 0f && uiState.state == TypewriterState.READY) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Continue",
                        tint = inkColor,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(chevronAlpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeBackContent(
    data: WelcomeBackData,
    alpha: Float,
    chevronAlpha: Float,
    inkColor: Color,
    pencilColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You\u2019ve been away.",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 28.sp,
                color = inkColor
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (data.entryCount > 0) {
            Text(
                text = "You have ${data.entryCount} ${if (data.entryCount == 1) "entry" else "entries"} and ${data.totalWords} words.",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 14.sp,
                    color = pencilColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Day 1 of a new practice.",
            style = TextStyle(
                fontFamily = CormorantGaramond,
                fontSize = 22.sp,
                fontStyle = FontStyle.Italic,
                color = inkColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Continue",
            tint = inkColor,
            modifier = Modifier
                .size(24.dp)
                .alpha(chevronAlpha)
        )
    }
}
