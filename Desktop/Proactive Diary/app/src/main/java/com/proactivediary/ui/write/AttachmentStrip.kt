package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proactivediary.data.media.ImageMetadata
import com.proactivediary.domain.model.TaggedContact
import java.io.File

/**
 * Unified attachment strip below the editor.
 * Only renders when there are actual attachments (images, audio, tags, contacts).
 * Horizontal scroll: [images] | [audio] | [tags] | [contacts]
 */
@Composable
fun AttachmentStrip(
    images: List<ImageMetadata>,
    thumbnailProvider: (String) -> File,
    audioPath: String?,
    isRecording: Boolean,
    tags: List<String>,
    taggedContacts: List<TaggedContact>,
    onImageClick: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onPlayAudio: () -> Unit,
    onDeleteAudio: () -> Unit,
    onEditTags: () -> Unit,
    onRemoveContact: (TaggedContact) -> Unit,
    secondaryTextColor: Color,
    textColor: Color,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    val hasContent = images.isNotEmpty() ||
            (audioPath != null && !isRecording) ||
            tags.isNotEmpty() ||
            taggedContacts.isNotEmpty()

    AnimatedVisibility(
        visible = hasContent,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Image thumbnails
            if (images.isNotEmpty()) {
                images.forEach { image ->
                    Box {
                        AsyncImage(
                            model = thumbnailProvider(image.filename),
                            contentDescription = "Photo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onImageClick(image.id) },
                            contentScale = ContentScale.Crop
                        )
                        // Small remove X
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .clickable { onRemoveImage(image.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(10.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Divider if more sections follow
                if (audioPath != null || tags.isNotEmpty() || taggedContacts.isNotEmpty()) {
                    StripDivider(secondaryTextColor)
                }
            }

            // Audio player (compact)
            if (audioPath != null && !isRecording) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = secondaryTextColor.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(onClick = onPlayAudio),
                            tint = textColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Voice",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        )
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable(onClick = onDeleteAudio),
                            tint = secondaryTextColor.copy(alpha = 0.4f)
                        )
                    }
                }

                if (tags.isNotEmpty() || taggedContacts.isNotEmpty()) {
                    StripDivider(secondaryTextColor)
                }
            }

            // Tags
            if (tags.isNotEmpty()) {
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = secondaryTextColor.copy(alpha = 0.06f),
                        modifier = Modifier.clickable(onClick = onEditTags)
                    ) {
                        Text(
                            text = "#$tag",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (taggedContacts.isNotEmpty()) {
                    StripDivider(secondaryTextColor)
                }
            }

            // Tagged contacts
            taggedContacts.forEach { contact ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = secondaryTextColor.copy(alpha = 0.06f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "@${contact.displayName}",
                            style = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        )
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onRemoveContact(contact) },
                            tint = secondaryTextColor.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StripDivider(color: Color) {
    Spacer(Modifier.width(4.dp))
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(24.dp)
            .background(color.copy(alpha = 0.15f))
    )
    Spacer(Modifier.width(4.dp))
}
