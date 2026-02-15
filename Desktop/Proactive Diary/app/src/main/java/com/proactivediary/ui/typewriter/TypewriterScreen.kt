package com.proactivediary.ui.typewriter

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onNavigateToDesignStudio: () -> Unit,
    onNavigateToMain: () -> Unit,
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

    // Navigate when fade-out completes — guard against double-fire during transition
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.screenAlpha, uiState.isNavigating) {
        if (!hasNavigated && viewModel.isNavigationComplete()) {
            hasNavigated = true
            if (uiState.isFirstLaunch) {
                onNavigateToDesignStudio()
            } else {
                onNavigateToMain()
            }
        }
    }

    // Suppress back button
    BackHandler { }

    if (!uiState.isLoaded) return

    // Start animation only after the screen is actually visible and composing
    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    // Cursor blink: step function, 530ms — only active during TYPING state
    val cursorBlinkOn by rememberCursorBlinkState(
        intervalMs = 530L,
        enabled = uiState.cursorVisible && uiState.state == TypewriterState.TYPING
    )

    val pencilColor = Color(0xFF585858)
    val inkColor = Color(0xFF313131)

    // Begin button pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "beginPulse")
    val beginPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beginPulseAlpha"
    )

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
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = TypewriterViewModel.CTA,
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 32.sp,
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

            // "Begin" pill button — appears after the CTA settles
            if (uiState.beginButtonAlpha > 0f) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .alpha(uiState.beginButtonAlpha * beginPulse)
                        .border(
                            width = 1.dp,
                            color = inkColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.onUserInteraction()
                        }
                        .padding(horizontal = 32.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Begin",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 16.sp,
                            color = inkColor
                        )
                    )
                }
            }
        }
    }
}
