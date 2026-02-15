package com.proactivediary.ui.write

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Jony Ive-style attachment tray that slides up from the bottom.
 * Replaces scattered inline widgets (photo, voice, template, tags, contacts).
 * Opened from the "+" button in the toolbar.
 */
@Composable
fun AttachmentTray(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit,
    onStartVoice: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenTags: () -> Unit,
    onOpenContacts: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    // Scrim + tray
    if (isVisible) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Semi-transparent scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )

            // Tray content
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = bgColor,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                    ) {
                        // Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .background(
                                    secondaryTextColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        Spacer(Modifier.height(20.dp))

                        // Row 1: Photo, Record, Guided
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrayItem(
                                icon = Icons.Outlined.CameraAlt,
                                label = "Photo",
                                textColor = textColor,
                                secondaryColor = secondaryTextColor,
                                onClick = {
                                    onAddPhoto()
                                    onDismiss()
                                }
                            )
                            TrayItem(
                                icon = Icons.Outlined.Mic,
                                label = "Dictate",
                                textColor = textColor,
                                secondaryColor = secondaryTextColor,
                                onClick = {
                                    onStartVoice()
                                    onDismiss()
                                }
                            )
                            TrayItem(
                                icon = Icons.Outlined.Description,
                                label = "Writing Templates",
                                textColor = textColor,
                                secondaryColor = secondaryTextColor,
                                onClick = {
                                    onOpenTemplates()
                                    onDismiss()
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Row 2: #Label, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrayItem(
                                icon = Icons.Outlined.Tag,
                                label = "Tags",
                                textColor = textColor,
                                secondaryColor = secondaryTextColor,
                                onClick = {
                                    onOpenTags()
                                    onDismiss()
                                }
                            )
                            TrayItem(
                                icon = Icons.Outlined.Person,
                                label = "Share",
                                textColor = textColor,
                                secondaryColor = secondaryTextColor,
                                onClick = {
                                    onOpenContacts()
                                    onDismiss()
                                }
                            )
                            // Empty spacer to maintain grid alignment
                            Box(modifier = Modifier.size(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrayItem(
    icon: ImageVector,
    label: String,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(72.dp)
            .background(
                secondaryColor.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = textColor.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontSize = 11.sp,
                color = secondaryColor
            )
        )
    }
}
