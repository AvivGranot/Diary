package com.proactivediary.ui.write

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.proactivediary.domain.model.TaggedContact

/**
 * Resolves a contact from the system contact picker URI.
 * The PickContact contract grants temporary read access to the selected contact,
 * so no READ_CONTACTS permission is needed.
 */
fun resolveContact(context: Context, contactUri: Uri): TaggedContact? {
    // Step 1: Get display name and contact ID
    val displayName: String
    val contactId: String

    context.contentResolver.query(
        contactUri,
        arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID),
        null, null, null
    )?.use { cursor ->
        if (!cursor.moveToFirst()) return null
        displayName = cursor.getString(0) ?: return null
        contactId = cursor.getString(1) ?: return null
    } ?: return null

    // Step 2: Get email (first available)
    val email = try {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId), null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: Exception) {
        null
    }

    // Step 3: Get phone (first available)
    val phone = try {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId), null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: Exception) {
        null
    }

    return TaggedContact(
        displayName = displayName,
        lookupUri = contactUri.toString(),
        email = email,
        phone = phone
    )
}

/**
 * Opens the Android share sheet pre-filled with the diary entry text,
 * targeting the contact's email if available.
 */
fun shareEntryWithContact(context: Context, state: WriteUiState, contact: TaggedContact) {
    val entryText = buildString {
        if (state.title.isNotBlank()) {
            appendLine(state.title)
            appendLine()
        }
        appendLine(state.content)
        appendLine()
        appendLine("---")
        appendLine("Written in Proactive Diary")
        appendLine("https://play.google.com/store/apps/details?id=com.proactivediary")
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, entryText)
        putExtra(Intent.EXTRA_SUBJECT, state.title.ifBlank { "My diary entry" })
        if (contact.email != null) {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(contact.email))
        }
    }

    context.startActivity(
        Intent.createChooser(shareIntent, "Share with ${contact.displayName}")
    )
}
