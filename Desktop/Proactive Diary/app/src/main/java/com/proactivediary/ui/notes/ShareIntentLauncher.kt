package com.proactivediary.ui.notes

import android.content.Context
import android.content.Intent
import com.proactivediary.domain.model.ShareChannel

object ShareIntentLauncher {

    fun buildShareText(noteContent: String): String {
        return if (noteContent.isNotBlank()) {
            "Someone sent you an anonymous note via Proactive Diary:\n\n\"$noteContent\"\n\nDownload the app to send one back: https://play.google.com/store/apps/details?id=com.proactivediary"
        } else {
            "Someone wants to send you a kind note on Proactive Diary! Download it: https://play.google.com/store/apps/details?id=com.proactivediary"
        }
    }

    fun launch(context: Context, channel: ShareChannel, noteContent: String): Boolean {
        val shareText = buildShareText(noteContent)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage(channel.packageName)
            if (channel == ShareChannel.GMAIL) {
                putExtra(Intent.EXTRA_SUBJECT, "Someone sent you an anonymous note!")
            }
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            // App not installed or can't handle intent — fallback to chooser
            try {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(fallback, "Share via"))
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
