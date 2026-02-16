package com.proactivediary.ui.quotes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proactivediary.ui.theme.DiaryColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    onBack: () -> Unit,
    viewModel: QuoteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DiaryColors.Ink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DiaryColors.Paper)
            )
        },
        containerColor = DiaryColors.Paper
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
                    CircularProgressIndicator(color = DiaryColors.ElectricIndigo)
                }
            } else if (state.quote != null) {
                val quote = state.quote!!

                // Quote content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "\u201C${quote.content}\u201D",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            lineHeight = 32.sp
                        ),
                        fontStyle = FontStyle.Italic,
                        color = DiaryColors.Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "\u2014 ${quote.authorName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiaryColors.Pencil,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = dateFormat.format(Date(quote.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = DiaryColors.Pencil.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 2.dp)
                    )

                    // Like button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleLike() }) {
                            Icon(
                                imageVector = if (state.isLiked) Icons.Default.Favorite
                                    else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (state.isLiked) DiaryColors.CoralRed
                                    else DiaryColors.Pencil,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "${quote.likeCount} likes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DiaryColors.Pencil
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = DiaryColors.Divider
                )

                // Comments header
                Text(
                    text = "Comments (${quote.commentCount})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DiaryColors.Ink,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )

                // Comments list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.comments, key = { it.id }) { comment ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DiaryColors.Parchment)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = DiaryColors.Ink
                                )
                                val commentDate = SimpleDateFormat("MMM d", Locale.getDefault())
                                Text(
                                    text = commentDate.format(Date(comment.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DiaryColors.Pencil.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DiaryColors.Ink,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    if (state.comments.isEmpty()) {
                        item {
                            Text(
                                text = "No comments yet. Be the first!",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = DiaryColors.Pencil,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }

                // Comment input
                HorizontalDivider(color = DiaryColors.Divider)
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
                            Text("Add a comment...", color = DiaryColors.Pencil.copy(alpha = 0.5f))
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = DiaryColors.Ink,
                            cursorColor = DiaryColors.ElectricIndigo
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
                                color = DiaryColors.ElectricIndigo
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                "Send",
                                tint = if (state.commentText.isNotBlank())
                                    DiaryColors.ElectricIndigo else DiaryColors.Pencil
                            )
                        }
                    }
                }

                if (state.commentError != null) {
                    Text(
                        text = state.commentError!!,
                        color = DiaryColors.CoralRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
