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

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
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

    // Always use createChooser — it wraps the share in a system activity
    // that properly returns to our app when the user presses back
    val chooser = Intent.createChooser(shareIntent, "Share your story")

    try {
        context.startActivity(chooser)
    } catch (_: android.content.ActivityNotFoundException) {
        // Target app not installed — clear package and retry with open chooser
        shareIntent.setPackage(null)
        context.startActivity(Intent.createChooser(shareIntent, "Share your story"))
    }
}
