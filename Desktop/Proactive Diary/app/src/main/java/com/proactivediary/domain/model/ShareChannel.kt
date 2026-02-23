package com.proactivediary.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ShareChannel(
    val key: String,
    val displayName: String,
    val packageName: String,
    val brandColor: Color,
    val icon: ImageVector
) {
    WHATSAPP("whatsapp", "WhatsApp", "com.whatsapp", Color(0xFF25D366), Icons.AutoMirrored.Outlined.Chat),
    INSTAGRAM("instagram", "Instagram", "com.instagram.android", Color(0xFFE4405F), Icons.Outlined.CameraAlt),
    MESSENGER("messenger", "Messenger", "com.facebook.orca", Color(0xFF0084FF), Icons.Outlined.Forum),
    X_TWITTER("x", "X", "com.twitter.android", Color(0xFF000000), Icons.Outlined.Tag),
    SNAPCHAT("snapchat", "Snapchat", "com.snapchat.android", Color(0xFFFFFC00), Icons.Outlined.PhotoCamera),
    LINKEDIN("linkedin", "LinkedIn", "com.linkedin.android", Color(0xFF0A66C2), Icons.Outlined.Work),
    MESSAGES("messages", "Messages", "com.google.android.apps.messaging", Color(0xFF1A73E8), Icons.Outlined.Sms),
    GMAIL("gmail", "Gmail", "com.google.android.gm", Color(0xFFEA4335), Icons.Outlined.Email),
    TELEGRAM("telegram", "Telegram", "org.telegram.messenger", Color(0xFF26A5E4), Icons.AutoMirrored.Outlined.Send);

    /** Icon tint: black for Snapchat (yellow bg), white for everything else */
    val iconTint: Color
        get() = if (this == SNAPCHAT) Color.Black else Color.White
}
