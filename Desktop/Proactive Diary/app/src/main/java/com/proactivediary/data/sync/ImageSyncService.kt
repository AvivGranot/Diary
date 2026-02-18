package com.proactivediary.data.sync

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.media.ImageMetadata
import com.proactivediary.data.media.ImageStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles uploading and downloading images and audio files to/from Firebase Storage.
 * Storage path: users/{uid}/images/{entryId}/{filename}
 */
@Singleton
class ImageSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val imageStorageManager: ImageStorageManager
) {
    companion object {
        private const val TAG = "ImageSyncService"
        private const val MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024 // 10MB
    }

    private val gson = Gson()
    private val uid: String? get() = auth.currentUser?.uid

    /**
     * Upload all images for an entry to Firebase Storage.
     * Parses the images JSON from the entry and uploads each file.
     */
    suspend fun uploadEntryImages(entryId: String, imagesJson: String) = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext
        if (imagesJson == "[]" || imagesJson.isBlank()) return@withContext

        val images: List<ImageMetadata> = try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(imagesJson, type)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse images JSON: ${e.message}")
            return@withContext
        }

        for (image in images) {
            try {
                // Upload full-size image
                val localFile = imageStorageManager.getImageFile(entryId, image.filename)
                if (localFile.exists() && localFile.length() < MAX_FILE_SIZE_BYTES) {
                    val ref = storage.reference
                        .child("users/$currentUid/images/$entryId/${image.filename}")
                    ref.putFile(android.net.Uri.fromFile(localFile)).await()
                }

                // Upload thumbnail
                val thumbFile = imageStorageManager.getThumbnailFile(entryId, image.filename)
                if (thumbFile.exists()) {
                    val thumbRef = storage.reference
                        .child("users/$currentUid/images/$entryId/thumbs/${image.filename}")
                    thumbRef.putFile(android.net.Uri.fromFile(thumbFile)).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload image ${image.filename}: ${e.message}")
                // Non-fatal: entry text is synced, image will retry
            }
        }
    }

    /**
     * Upload audio file for an entry to Firebase Storage.
     */
    suspend fun uploadEntryAudio(entryId: String, audioPath: String?) = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext
        if (audioPath.isNullOrBlank()) return@withContext

        try {
            val localFile = File(audioPath)
            if (localFile.exists() && localFile.length() < MAX_FILE_SIZE_BYTES) {
                val ref = storage.reference
                    .child("users/$currentUid/audio/$entryId/${localFile.name}")
                ref.putFile(android.net.Uri.fromFile(localFile)).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload audio: ${e.message}")
        }
    }

    /**
     * Download all images for an entry from Firebase Storage (used during restore).
     */
    suspend fun downloadEntryImages(entryId: String, imagesJson: String) = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext
        if (imagesJson == "[]" || imagesJson.isBlank()) return@withContext

        val images: List<ImageMetadata> = try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(imagesJson, type)
        } catch (e: Exception) {
            return@withContext
        }

        for (image in images) {
            try {
                // Download full-size image
                val localFile = imageStorageManager.getImageFile(entryId, image.filename)
                if (!localFile.exists()) {
                    localFile.parentFile?.mkdirs()
                    val ref = storage.reference
                        .child("users/$currentUid/images/$entryId/${image.filename}")
                    ref.getFile(localFile).await()
                }

                // Download thumbnail
                val thumbFile = imageStorageManager.getThumbnailFile(entryId, image.filename)
                if (!thumbFile.exists()) {
                    thumbFile.parentFile?.mkdirs()
                    val thumbRef = storage.reference
                        .child("users/$currentUid/images/$entryId/thumbs/${image.filename}")
                    thumbRef.getFile(thumbFile).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download image ${image.filename}: ${e.message}")
                // Non-fatal: entry is visible, image placeholder shown
            }
        }
    }

    /**
     * Download audio file for an entry from Firebase Storage (used during restore).
     */
    suspend fun downloadEntryAudio(entryId: String, audioPath: String?) = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext
        if (audioPath.isNullOrBlank()) return@withContext

        try {
            val localFile = File(audioPath)
            if (!localFile.exists()) {
                localFile.parentFile?.mkdirs()
                val ref = storage.reference
                    .child("users/$currentUid/audio/$entryId/${localFile.name}")
                ref.getFile(localFile).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to download audio: ${e.message}")
        }
    }

    /**
     * Delete all images and audio for an entry from Firebase Storage.
     */
    suspend fun deleteEntryMedia(entryId: String) = withContext(Dispatchers.IO) {
        val currentUid = uid ?: return@withContext
        try {
            // List and delete all files under the entry's images path
            val imagesRef = storage.reference.child("users/$currentUid/images/$entryId")
            val items = imagesRef.listAll().await()
            for (item in items.items) {
                item.delete().await()
            }
            // Also delete thumbs subfolder
            for (prefix in items.prefixes) {
                val subItems = prefix.listAll().await()
                for (subItem in subItems.items) {
                    subItem.delete().await()
                }
            }

            // Delete audio
            val audioRef = storage.reference.child("users/$currentUid/audio/$entryId")
            val audioItems = audioRef.listAll().await()
            for (item in audioItems.items) {
                item.delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete entry media: ${e.message}")
        }
    }
}
