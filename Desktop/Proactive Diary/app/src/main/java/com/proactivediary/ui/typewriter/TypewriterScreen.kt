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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.MaterialTheme
import com.proactivediary.ui.theme.PlusJakartaSans

@Composable
fun TypewriterScreen(
    onNavigateToDesignStudio: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: TypewriterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    // Immersive mode
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

    BackHandler { }

    if (!uiState.isLoaded) return

    LaunchedEffect(Unit) {
        viewModel.onScreenVisible()
    }

    // Cursor blink
    val cursorBlinkOn by rememberCursorBlinkState(
        intervalMs = 530L,
        enabled = uiState.cursorVisible && uiState.state == TypewriterState.TYPING
    )

    // Breathing gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    // Button pulse
    val beginPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beginPulseAlpha"
    )

    // Dynamic gradient background — dark with subtle blue glow
    val accentColor = MaterialTheme.colorScheme.primary
    val darkBase = Color(0xFF000000)
    val darkMid = Color(0xFF0A0A0A)
    val accentGlow = accentColor.copy(alpha = 0.08f + 0.04f * gradientShift)
    val gradientColors = listOf(darkBase, darkMid, accentGlow, darkBase)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .graphicsLayer { alpha = uiState.screenAlpha }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (uiState.state == TypewriterState.READY) {
                    viewModel.onUserInteraction()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon/emoji
            Text(
                text = "\u270F\uFE0F",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Typewriter quote — still shows the animated text
            TypewriterCanvas(
                quote = TypewriterViewModel.QUOTE,
                visibleCharCount = uiState.visibleCharCount,
                cursorVisible = uiState.cursorVisible,
                cursorAlpha = uiState.cursorAlpha,
                cursorBlinkOn = cursorBlinkOn
            )

            // Attribution
            if (uiState.attributionAlpha > 0f) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = TypewriterViewModel.ATTRIBUTION,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.7.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.alpha(uiState.attributionAlpha)
                )
            }

            // CTA
            if (uiState.ctaAlpha > 0f) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = TypewriterViewModel.CTA,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(uiState.ctaAlpha)
                        .graphicsLayer {
                            translationY = uiState.ctaTranslateY * density
                        }
                )
            }

            // Begin button — blue accent
            if (uiState.beginButtonAlpha > 0f) {
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .alpha(uiState.beginButtonAlpha * beginPulse)
                        .fillMaxWidth(0.6f)
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.onUserInteraction()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start Writing",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

/** Helper to lerp between colors for gradient animation */
private fun Color.Companion.lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}
