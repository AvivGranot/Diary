package com.proactivediary.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.proactivediary.R

enum class ShareChannel(
    val key: String,
    val displayName: String,
    val packageName: String,
    val brandColor: Color,
    @DrawableRes val iconRes: Int
) {
    WHATSAPP("whatsapp", "WhatsApp", "com.whatsapp", Color(0xFF25D366), R.drawable.ic_whatsapp),
    INSTAGRAM("instagram", "Instagram", "com.instagram.android", Color(0xFFE4405F), R.drawable.ic_instagram),
    MESSENGER("messenger", "Messenger", "com.facebook.orca", Color(0xFF0084FF), R.drawable.ic_messenger),
    X_TWITTER("x", "X", "com.twitter.android", Color(0xFF000000), R.drawable.ic_x_twitter),
    SNAPCHAT("snapchat", "Snapchat", "com.snapchat.android", Color(0xFFFFFC00), R.drawable.ic_snapchat),
    TIKTOK("tiktok", "TikTok", "com.zhiliaoapp.musically", Color(0xFF000000), R.drawable.ic_tiktok),
    LINKEDIN("linkedin", "LinkedIn", "com.linkedin.android", Color(0xFF0A66C2), R.drawable.ic_linkedin),
    MESSAGES("messages", "Messages", "com.google.android.apps.messaging", Color(0xFF1A73E8), R.drawable.ic_messages),
    GMAIL("gmail", "Gmail", "com.google.android.gm", Color(0xFFEA4335), R.drawable.ic_gmail),
    TELEGRAM("telegram", "Telegram", "org.telegram.messenger", Color(0xFF26A5E4), R.drawable.ic_telegram);

    /** Icon tint: black for Snapchat (yellow bg), white for everything else */
    val iconTint: Color
        get() = if (this == SNAPCHAT) Color.Black else Color.White
}
