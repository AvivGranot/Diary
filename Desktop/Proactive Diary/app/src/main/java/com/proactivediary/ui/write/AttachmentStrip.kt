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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.domain.model.TaggedContact

/**
 * Unified attachment strip below the editor.
 * Only renders when there are actual attachments (tags, contacts).
 * Horizontal scroll: [tags] | [contacts]
 */
@Composable
fun AttachmentStrip(
    tags: List<String> = emptyList(),
    taggedContacts: List<TaggedContact>,
    onEditTags: () -> Unit = {},
    onRemoveContact: (TaggedContact) -> Unit,
    secondaryTextColor: Color,
    textColor: Color,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    val hasContent = taggedContacts.isNotEmpty()

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
