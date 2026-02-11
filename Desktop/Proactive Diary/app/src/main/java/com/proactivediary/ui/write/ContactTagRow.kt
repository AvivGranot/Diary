package com.proactivediary.ui.write

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.TaggedContact

@Composable
fun ContactTagRow(
    taggedContacts: List<TaggedContact>,
    textTags: List<String>,
    onAddContactClick: () -> Unit,
    onRemoveContact: (TaggedContact) -> Unit,
    onShareWithContact: (TaggedContact) -> Unit,
    onTextTagsClick: () -> Unit,
    secondaryTextColor: Color,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "#" text tag button (moved from toolbar)
        Text(
            text = if (textTags.isEmpty()) "#" else textTags.joinToString(" ") { "#$it" },
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor
            ),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onTextTagsClick() }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        )

        // "@" contact tag button
        Text(
            text = "@",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor
            ),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddContactClick() }
                .padding(vertical = 4.dp, horizontal = 4.dp)
        )

        // Contact chips
        taggedContacts.forEach { contact ->
            ContactChip(
                contact = contact,
                onRemove = { onRemoveContact(contact) },
                onShare = { onShareWithContact(contact) },
                secondaryTextColor = secondaryTextColor
            )
        }
    }
}

@Composable
private fun ContactChip(
    contact: TaggedContact,
    onRemove: () -> Unit,
    onShare: () -> Unit,
    secondaryTextColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = secondaryTextColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
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

            // Send icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Share with ${contact.displayName}",
                modifier = Modifier
                    .size(14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShare() },
                tint = secondaryTextColor.copy(alpha = 0.6f)
            )

            // Remove icon
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove ${contact.displayName}",
                modifier = Modifier
                    .size(14.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRemove() },
                tint = secondaryTextColor.copy(alpha = 0.4f)
            )
        }
    }
}
