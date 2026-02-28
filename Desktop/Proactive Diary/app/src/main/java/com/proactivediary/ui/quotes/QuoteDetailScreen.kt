package com.proactivediary.ui.quotes

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.quotes.components.AuthorAvatar
import com.proactivediary.ui.quotes.components.ProfileDialogData
import com.proactivediary.ui.quotes.components.ProfilePhotoDialog
import com.proactivediary.ui.theme.InstrumentSerif

private val Gold = Color(0xFFFFD700)
private val RedAccent = Color(0xFFFF3B5C)
private val CommentPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    onBack: () -> Unit,
    viewModel: QuoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var profileDialogData by remember { mutableStateOf<ProfileDialogData?>(null) }

    if (profileDialogData != null) {
        ProfilePhotoDialog(data = profileDialogData!!, onDismiss = { profileDialogData = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (state.quote != null) {
                val quote = state.quote!!

                // Determine rank from sample quotes
                val rank = QuotesViewModel.SAMPLE_QUOTES.indexOfFirst { it.id == quote.id }
                    .let { if (it >= 0) it + 1 else -1 }

                // ── Scrollable content ──
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Rank badge ──
                    if (rank in 1..10) {
                        item(key = "rank_badge") {
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Gold.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "\uD83C\uDFC6 #$rank This Week",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                        item(key = "top_spacer") {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }

                    // ── Quote text ──
                    item(key = "quote_text") {
                        Text(
                            text = "\u201C${quote.content}\u201D",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = InstrumentSerif,
                                lineHeight = 36.sp
                            ),
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // ── Author row: avatar + name + time ──
                    item(key = "author") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuthorAvatar(
                                photoUrl = quote.authorPhotoUrl,
                                authorName = quote.authorName,
                                size = 36,
                                onClick = { profileDialogData = ProfileDialogData(quote.authorName, quote.authorPhotoUrl) }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${quote.authorName} \u00B7 ${formatTimeAgo(quote.createdAt)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // ── Action buttons: Heart, Comment, Share ──
                    item(key = "actions") {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Heart
                            ActionCircleButton(
                                icon = if (state.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                label = "${quote.likeCount}",
                                backgroundColor = RedAccent,
                                iconTint = Color.White,
                                onClick = { viewModel.toggleLike() }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            // Comment
                            ActionCircleButton(
                                icon = Icons.Default.ChatBubbleOutline,
                                label = "${quote.commentCount}",
                                backgroundColor = CommentPurple,
                                iconTint = Color.White,
                                onClick = { /* scroll to comments */ }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            // Share
                            ActionCircleButton(
                                icon = Icons.Default.Share,
                                label = "Share",
                                backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                iconTint = MaterialTheme.colorScheme.onBackground,
                                onClick = {
                                    val shareText = "\u201C${quote.content}\u201D\n\u2014 ${quote.authorName}\n\nShared from Proactive Diary"
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share quote"))
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // ── Comments section ──
                    item(key = "comments_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state.comments.isEmpty()) {
                        item(key = "no_comments") {
                            Text(
                                text = "No comments yet. Be the first!",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                        }
                    } else {
                        items(state.comments, key = { it.id }) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AuthorAvatar(
                                    photoUrl = null,
                                    authorName = comment.authorName,
                                    size = 28,
                                    onClick = { profileDialogData = ProfileDialogData(comment.authorName, null) }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = comment.authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // ── Comment input bar (pinned at bottom) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = state.commentText,
                                onValueChange = { viewModel.updateCommentText(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Add a comment...",
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.submitComment() },
                                enabled = state.commentText.isNotBlank() && !state.isSendingComment
                            ) {
                                if (state.isSendingComment) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        "Send",
                                        tint = if (state.commentText.isNotBlank())
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }

                        if (state.commentError != null) {
                            Text(
                                text = state.commentError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Quote not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quote not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ── Large circular action button (heart, comment, share) ──
@Composable
private fun ActionCircleButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

/** Format timestamp to relative time ("1h ago", "3h ago", "2d ago") */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp
    val minutes = diffMs / 60_000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
