package com.proactivediary.ui.quotes

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.data.social.Quote
import com.proactivediary.ui.quotes.components.AuthorAvatar
import com.proactivediary.ui.theme.InstrumentSerif
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Theme colors
private val AccentBlue = Color(0xFF3B82F6)
private val AccentBlueGlow = Color(0xFF3B82F6).copy(alpha = 0.12f)
private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)
private val RedAccent = Color(0xFFFF3B5C)
private val BlueGradientStart = Color(0xFF60A5FA)
private val BlueGradientEnd = Color(0xFF2563EB)

@Composable
fun QuotesScreen(
    onQuoteClick: (String) -> Unit,
    onSendNote: () -> Unit = {},
    unreadNoteCount: Int = 0,
    onNotificationBellClick: () -> Unit = {},
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header: + button | centered title | notification bell ──
            item(key = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.showComposeSheet() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Write a quote",
                            modifier = Modifier.size(24.dp),
                            tint = AccentBlue
                        )
                    }

                    Text(
                        text = "Quotes",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = InstrumentSerif,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onNotificationBellClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNoteCount > 0) {
                                    Badge {
                                        Text(
                                            text = if (unreadNoteCount > 9) "9+" else "$unreadNoteCount",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notes Inbox",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // ── Stories Row ──
            item(key = "stories_row") {
                StoriesRow(
                    quotes = state.trendingQuotes,
                    onYourQuoteTap = { viewModel.showComposeSheet() },
                    onAuthorTap = { /* future: scroll to author's quote */ }
                )
            }

            // ── Tab pills: Trending / New / Following (centered) ──
            item(key = "tabs") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val tabOrder = listOf(QuotesTab.TRENDING, QuotesTab.NEW, QuotesTab.FOLLOWING)
                    tabOrder.forEachIndexed { index, tab ->
                        val isSelected = state.selectedTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) AccentBlue
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.selectTab(tab) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = when (tab) {
                                    QuotesTab.TRENDING -> "\uD83D\uDD25 Trending"
                                    QuotesTab.NEW -> "New"
                                    QuotesTab.FOLLOWING -> "Following"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (index < tabOrder.lastIndex) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Weekly Leaderboard ──
            if (state.trendingQuotes.isNotEmpty()) {
                item(key = "leaderboard_label") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\uD83C\uDFC6 WEEKLY LEADERBOARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = Gold
                        )
                        Text(
                            text = "Swipe to see top 10 \u2192",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item(key = "leaderboard_cards") {
                    LeaderboardCardsRow(
                        quotes = state.trendingQuotes.take(10),
                        onQuoteClick = { onQuoteClick(it.id) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ── Quote Feed ──
            val currentQuotes = when (state.selectedTab) {
                QuotesTab.TRENDING -> state.trendingQuotes
                QuotesTab.NEW -> state.newQuotes
                QuotesTab.FOLLOWING -> state.myQuotes
            }

            if (currentQuotes.isEmpty() && !state.isLoading) {
                item(key = "empty") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp, bottom = 80.dp)
                    ) {
                        Text(
                            text = "The stage is yours",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Write up to 25 words and inspire thousands",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                items(currentQuotes, key = { it.id }) { quote ->
                    FeedPostCard(
                        quote = quote,
                        isLiked = state.likedQuoteIds.contains(quote.id),
                        onLike = { viewModel.toggleLike(quote.id) },
                        onClick = { onQuoteClick(quote.id) },
                        onShare = {
                            val shareText = "\u201C${quote.content}\u201D\n\u2014 ${quote.authorName}\n\nShared from Proactive Diary"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share quote"))
                        }
                    )
                }

                // Bottom spacer for nav bar clearance
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    // Compose quote bottom sheet
    if (state.composeSheetVisible) {
        ComposeQuoteSheet(
            content = state.composeContent,
            wordCount = state.composeWordCount,
            isSubmitting = state.isSubmitting,
            error = state.submitError,
            onContentChange = { viewModel.updateComposeContent(it) },
            onSubmit = { viewModel.submitQuote() },
            onDismiss = { viewModel.hideComposeSheet() }
        )
    }
}

// ── Stories Row ──
@Composable
private fun StoriesRow(
    quotes: List<Quote>,
    onYourQuoteTap: () -> Unit,
    onAuthorTap: (String) -> Unit
) {
    val uniqueAuthors = remember(quotes) {
        quotes.distinctBy { it.authorId }.take(15)
    }

    val blueGradient = remember {
        Brush.sweepGradient(listOf(BlueGradientStart, BlueGradientEnd, BlueGradientStart))
    }

    Column {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "You" story — always first
            item(key = "story_you") {
                StoryItem(
                    label = "Your quote",
                    photoUrl = null,
                    authorName = "+",
                    ringBrush = blueGradient,
                    isYourStory = true,
                    onClick = onYourQuoteTap
                )
            }

            // Friend stories from trending authors
            items(uniqueAuthors, key = { "story_${it.authorId}" }) { quote ->
                StoryItem(
                    label = quote.authorName.split(" ").first(),
                    photoUrl = quote.authorPhotoUrl,
                    authorName = quote.authorName,
                    ringBrush = blueGradient,
                    isYourStory = false,
                    onClick = { onAuthorTap(quote.authorId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ── Single Story Item ──
@Composable
private fun StoryItem(
    label: String,
    photoUrl: String?,
    authorName: String,
    ringBrush: Brush,
    isYourStory: Boolean,
    onClick: () -> Unit
) {
    val ringStrokeWidth = 2.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable(onClick = onClick)
    ) {
        // Outer ring with gradient stroke
        Box(
            modifier = Modifier
                .size(56.dp)
                .drawBehind {
                    drawCircle(
                        brush = ringBrush,
                        radius = size.minDimension / 2,
                        style = Stroke(width = ringStrokeWidth.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isYourStory) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add your quote",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                AuthorAvatar(
                    photoUrl = photoUrl,
                    authorName = authorName,
                    size = 48
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(52.dp)
        )
    }
}

// ── Horizontal Leaderboard Cards ──
@Composable
private fun LeaderboardCardsRow(
    quotes: List<Quote>,
    onQuoteClick: (Quote) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(quotes, key = { _, q -> "lb_${q.id}" }) { index, quote ->
            val isFirst = index == 0
            val rankColor = when (index) {
                0 -> Gold
                1 -> Silver
                2 -> Bronze
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }

            Column(
                modifier = Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isFirst) Modifier
                            .background(AccentBlueGlow)
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        else Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(14.dp)
                            )
                    )
                    .clickable { onQuoteClick(quote) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(rankColor, CircleShape)
                        .align(Alignment.Start),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Author avatar
                AuthorAvatar(
                    photoUrl = quote.authorPhotoUrl,
                    authorName = quote.authorName,
                    size = 44
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Username
                Text(
                    text = quote.authorName.split(" ").firstOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Quote text
                Text(
                    text = "\u201C${quote.content}\u201D",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InstrumentSerif,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Like count
                Text(
                    text = "\u2764\uFE0F ${quote.likeCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = if (isFirst) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isFirst) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ── Feed Post Card (Twitter-style) ──
@Composable
private fun FeedPostCard(
    quote: Quote,
    isLiked: Boolean,
    onLike: () -> Unit,
    onClick: () -> Unit,
    onShare: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header: avatar + name + @username + timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AuthorAvatar(
                photoUrl = quote.authorPhotoUrl,
                authorName = quote.authorName,
                size = 32
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = quote.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${quote.authorName.lowercase().replace(" ", "")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
            Text(
                text = dateFormat.format(Date(quote.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quote text — Instrument Serif, larger
        Text(
            text = "\u201C${quote.content}\u201D",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = InstrumentSerif,
                fontSize = 18.sp,
                lineHeight = 26.sp
            ),
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Actions row: heart, comment, share, bookmark
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onLike)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${quote.likeCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Comment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${quote.commentCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Share
            Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onShare)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bookmark
            Icon(
                Icons.Outlined.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        // Divider
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        )
    }
}
