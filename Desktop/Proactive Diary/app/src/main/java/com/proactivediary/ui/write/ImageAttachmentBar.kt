package com.proactivediary.ui.write

import android.net.Uri
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.proactivediary.data.media.ImageMetadata
import java.io.File

/**
 * Horizontal scrollable bar showing image thumbnails with an add button.
 * Only visible when there are images or user is on Write screen.
 */
@Composable
fun ImageAttachmentBar(
    images: List<ImageMetadata>,
    thumbnailProvider: (String) -> File,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) {
        // Show a subtle add photo button when no images
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = "Add photo",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onAddClick),
                tint = secondaryTextColor.copy(alpha = 0.4f)
            )
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        images.forEach { image ->
            Box {
                AsyncImage(
                    model = thumbnailProvider(image.filename),
                    contentDescription = "Attached photo",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(image.id) },
                    contentScale = ContentScale.Crop
                )
                // Remove button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .clickable { onRemoveClick(image.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }

        // Add more button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(secondaryTextColor.copy(alpha = 0.08f))
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = "Add photo",
                modifier = Modifier.size(24.dp),
                tint = secondaryTextColor.copy(alpha = 0.5f)
            )
        }
    }
}
