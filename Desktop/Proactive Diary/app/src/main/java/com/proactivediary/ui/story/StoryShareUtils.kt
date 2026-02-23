package com.proactivediary.ui.story

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.proactivediary.domain.model.ShareChannel
import java.io.File

fun shareStoryImage(context: Context, imageFile: File, channel: ShareChannel? = null) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(
            Intent.EXTRA_TEXT,
            "Created with Proactive Diary — https://play.google.com/store/apps/details?id=com.proactivediary"
        )
        type = "image/png"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (channel != null) {
            setPackage(channel.packageName)
        }
    }

    try {
        if (channel != null) {
            context.startActivity(shareIntent)
        } else {
            context.startActivity(Intent.createChooser(shareIntent, "Share your story"))
        }
    } catch (_: android.content.ActivityNotFoundException) {
        // App not installed — fall back to system chooser
        val fallback = Intent.createChooser(
            shareIntent.apply { setPackage(null) },
            "Share your story"
        )
        context.startActivity(fallback)
    }
}
