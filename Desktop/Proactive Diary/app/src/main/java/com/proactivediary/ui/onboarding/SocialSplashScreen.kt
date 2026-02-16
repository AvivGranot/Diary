package com.proactivediary.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.DiaryColors
import kotlinx.coroutines.launch

// Red envelope color — unified body + flap
private val EnvelopeRed = Color(0xFFC0392B)

@Composable
fun SocialSplashScreen(
    onContinue: () -> Unit,
    viewModel: SocialSplashViewModel = hiltViewModel()
) {
    val userCount by viewModel.userCount.collectAsState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isOpened by remember { mutableStateOf(false) }

    // ── State 1 animations: envelope entry ──
    val envelopeAlpha = remember { Animatable(0f) }
    val envelopeScale = remember { Animatable(0.9f) }

    // ── State 2 animations: open sequence ──
    val flapRotation = remember { Animatable(0f) }
    val letterOffsetDp = remember { Animatable(100f) }  // starts 100dp below, slides to -20dp
    val letterAlpha = remember { Animatable(0f) }
    val proofAlpha = remember { Animatable(0f) }
    val ctaAlpha = remember { Animatable(0f) }

    // Entry animation
    LaunchedEffect(Unit) {
        launch { envelopeAlpha.animateTo(1f, tween(600, easing = EaseOutCubic)) }
        envelopeScale.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    // Envelope dimensions
    val envelopeWidth = 240.dp
    val envelopeHeight = 170.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp)
        ) {
            // ── Envelope + letter container ──
            Box(
                modifier = Modifier
                    .size(envelopeWidth, envelopeHeight + 120.dp) // extra room for letter to slide above
                    .graphicsLayer {
                        scaleX = envelopeScale.value
                        scaleY = envelopeScale.value
                        alpha = envelopeAlpha.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // ── Letter card (slides up when opened) ──
                if (isOpened) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(0, with(density) { letterOffsetDp.value.dp.roundToPx() })
                            }
                            .alpha(letterAlpha.value)
                            .size(envelopeWidth - 24.dp, 140.dp)
                            .shadow(8.dp, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Someone thinks\nyou\u2019re incredible.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                lineHeight = 32.sp
                            ),
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            color = DiaryColors.Ink,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ── Envelope body (Canvas) ──
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 40.dp) // push envelope down so letter has room above
                        .size(envelopeWidth, envelopeHeight)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!isOpened) {
                                isOpened = true
                                scope.launch {
                                    // Flap opens
                                    launch {
                                        flapRotation.animateTo(
                                            180f,
                                            tween(600, easing = EaseOutCubic)
                                        )
                                    }
                                    // Letter slides up after slight delay
                                    launch {
                                        kotlinx.coroutines.delay(300)
                                        launch {
                                            letterAlpha.animateTo(1f, tween(300))
                                        }
                                        letterOffsetDp.animateTo(
                                            -20f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    // Social proof + CTA fade in
                                    launch {
                                        kotlinx.coroutines.delay(800)
                                        launch {
                                            proofAlpha.animateTo(1f, tween(500))
                                        }
                                        kotlinx.coroutines.delay(200)
                                        ctaAlpha.animateTo(1f, tween(500))
                                    }
                                }
                            }
                        }
                ) {
                    val cornerPx = with(density) { 12.dp.toPx() }
                    val flapTipY = with(density) { envelopeHeight.toPx() } * 0.38f

                    // Draw envelope body
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Body: red rounded rect
                        drawRoundRect(
                            color = EnvelopeRed,
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    }

                    // Text on envelope body (visible only when closed)
                    if (!isOpened) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 60.dp, start = 20.dp, end = 20.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "Someone thinks\nyou\u2019re incredible.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 22.sp
                                ),
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFFAF6EE).copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // V-flap with rotation animation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { (flapTipY / density.density).dp })
                            .graphicsLayer {
                                rotationX = flapRotation.value
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                                cameraDistance = 12f * density.density
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw flap: darker red V-triangle with rounded top
                            drawRoundRect(
                                color = EnvelopeRed,
                                topLeft = Offset(0f, 0f),
                                size = Size(w, cornerPx * 2),
                                cornerRadius = CornerRadius(cornerPx, cornerPx)
                            )

                            val flapPath = Path().apply {
                                moveTo(0f, cornerPx * 0.5f)
                                lineTo(w / 2f, h)
                                lineTo(w, cornerPx * 0.5f)
                                close()
                            }
                            drawPath(flapPath, color = EnvelopeRed)
                        }
                    }

                }
            }

            // ── "Tap to open" hint (only when closed) ──
            if (!isOpened) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to open",
                    fontSize = 13.sp,
                    color = DiaryColors.Pencil.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(envelopeAlpha.value)
                )
            }

            // ── Social proof (after open) ──
            if (isOpened) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = if (userCount > 0)
                        "Join ${userCount}+ people sending anonymous kindness"
                    else
                        "Send anonymous kindness to the people you care about",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiaryColors.Pencil,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .alpha(proofAlpha.value)
                )
            }

            // ── CTA button (after open) ──
            if (isOpened) {
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .alpha(ctaAlpha.value)
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiaryColors.Ink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Open Your Notes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
