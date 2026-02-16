package com.proactivediary.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.notes.components.NoteContentCard
import com.proactivediary.ui.theme.DiaryColors
import kotlinx.coroutines.delay

@Composable
fun SocialSplashScreen(
    onContinue: () -> Unit,
    viewModel: SocialSplashViewModel = hiltViewModel()
) {
    val userCount by viewModel.userCount.collectAsState()

    // Animations
    val envelopeScale = remember { Animatable(0.5f) }
    val envelopeAlpha = remember { Animatable(0f) }
    val messageAlpha = remember { Animatable(0f) }
    val ctaAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Envelope fades in and scales up
        envelopeAlpha.animateTo(1f, tween(600))
        envelopeScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )

        // Step 2: Message reveals
        delay(300)
        messageAlpha.animateTo(1f, tween(600))

        // Step 3: CTA appears
        delay(400)
        ctaAlpha.animateTo(1f, tween(400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiaryColors.Paper),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Envelope with note
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = envelopeScale.value
                        scaleY = envelopeScale.value
                    }
                    .alpha(envelopeAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAF6EE))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Wax seal
                        Box(
                            modifier = Modifier
                                .background(DiaryColors.ElectricIndigo, RoundedCornerShape(50))
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "For You",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Revealed message
                        Box(modifier = Modifier.alpha(messageAlpha.value)) {
                            NoteContentCard(
                                content = "Someone thinks you\u2019re incredible."
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Social proof
            Box(modifier = Modifier.alpha(messageAlpha.value)) {
                if (userCount > 0) {
                    Text(
                        text = "Join ${userCount}+ people sending anonymous kindness",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiaryColors.Pencil,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Send anonymous kindness to the people you care about",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiaryColors.Pencil,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTA
            Box(modifier = Modifier.alpha(ctaAlpha.value)) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiaryColors.ElectricIndigo
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "See Who Sent This",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
