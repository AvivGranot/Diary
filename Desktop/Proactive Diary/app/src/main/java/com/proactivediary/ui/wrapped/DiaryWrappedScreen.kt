package com.proactivediary.ui.wrapped

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.CormorantGaramond

private val Cream = Color(0xFFF3EEE7)
private val Ink = Color(0xFF313131)
private val InkLight = Color(0xFF787878)

@Composable
fun DiaryWrappedScreen(
    onBack: () -> Unit,
    viewModel: DiaryWrappedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Ink, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Preparing your story\u2026",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkLight
                    )
                )
            }
        }
        return
    }

    if (state.cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(48.dp)
            ) {
                Text(
                    text = "Not enough entries yet",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 22.sp,
                        color = Ink
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Write more to unlock your Diary Wrapped",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkLight
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "\u2190 Back",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 14.sp,
                        color = InkLight
                    ),
                    modifier = Modifier.clickable(onClick = onBack)
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.cards.size }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .navigationBarsPadding()
    ) {
        // Top bar: back + share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink
                )
            }

            Text(
                text = "Your ${java.time.LocalDate.now().year} Wrapped",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 18.sp,
                    color = Ink
                )
            )

            IconButton(onClick = {
                // Share current card as image
                // This is a simplified share — shares text summary
                val card = state.cards.getOrNull(pagerState.currentPage) ?: return@IconButton
                val shareText = buildString {
                    append("${card.headline}\n")
                    if (card.bigNumber.isNotBlank()) append("${card.bigNumber} ${card.subtitle}\n")
                    if (card.detail.isNotBlank()) append("\n${card.detail}")
                    append("\n\nWritten in Proactive Diary")
                }
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share your Wrapped"))
            }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = Ink
                )
            }
        }

        // Swipeable cards
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) { page ->
            val card = state.cards[page]
            WrappedCardView(card = card)
        }

        // Instagram Stories-style progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            state.cards.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Ink.copy(alpha = 0.12f))
                ) {
                    if (index <= pagerState.currentPage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(1.dp))
                                .background(Ink.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }

        // "Swipe to continue" hint on first page
        if (pagerState.currentPage == 0) {
            Text(
                text = "Swipe to see your story \u2192",
                style = TextStyle(
                    fontFamily = CormorantGaramond,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = InkLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WrappedCardView(card: WrappedCard) {
    val cardBg = Color(card.accentColorHex)
    val textColor = Color.White
    val subtextColor = Color.White.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (card.type) {
                WrappedCardType.INTRO -> {
                    Text(
                        text = card.headline,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = card.subtitle,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                WrappedCardType.CLOSING -> {
                    Text(
                        text = card.headline,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = card.subtitle,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = subtextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = card.detail,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 14.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Proactive Diary",
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            color = subtextColor.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    )
                }

                WrappedCardType.FIRST_ENTRY -> {
                    Text(
                        text = card.headline,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 18.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = card.subtitle,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = subtextColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = card.detail,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 18.sp,
                            fontStyle = FontStyle.Italic,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        ),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                else -> {
                    // Standard big-number card layout
                    Text(
                        text = card.headline,
                        style = TextStyle(
                            fontFamily = CormorantGaramond,
                            fontSize = 18.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(Modifier.height(16.dp))

                    if (card.bigNumber.isNotBlank()) {
                        val isShort = card.bigNumber.length <= 4
                        // Try count-up animation for numeric values
                        val numericValue = card.bigNumber.replace(",", "").toIntOrNull()
                        if (numericValue != null && numericValue > 0) {
                            val animatable = remember(card.bigNumber) { Animatable(0f) }
                            LaunchedEffect(card.bigNumber) {
                                animatable.animateTo(
                                    targetValue = numericValue.toFloat(),
                                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.US, "%,d", animatable.value.toInt()),
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontSize = if (isShort) 72.sp else 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        } else {
                            Text(
                                text = card.bigNumber,
                                style = TextStyle(
                                    fontFamily = CormorantGaramond,
                                    fontSize = if (isShort) 72.sp else 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }

                    if (card.subtitle.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = card.subtitle,
                            style = TextStyle(
                                fontFamily = CormorantGaramond,
                                fontSize = 16.sp,
                                color = subtextColor,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    if (card.detail.isNotBlank()) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = card.detail,
                            style = TextStyle(
                                fontFamily = CormorantGaramond,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = subtextColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
