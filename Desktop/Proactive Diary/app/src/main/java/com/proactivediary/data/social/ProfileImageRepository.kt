package com.proactivediary.data.social

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Compress image from URI to 512px max / 80% JPEG, upload to Firebase Storage,
     * then update Firestore user doc with the download URL.
     */
    suspend fun uploadAvatar(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("Not signed in"))

            // Decode with inJustDecodeBounds
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate sample size for max 512px
            val maxDim = 512
            var sampleSize = 1
            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth > maxDim || origHeight > maxDim) {
                val halfWidth = origWidth / 2
                val halfHeight = origHeight / 2
                while ((halfWidth / sampleSize) >= maxDim || (halfHeight / sampleSize) >= maxDim) {
                    sampleSize *= 2
                }
            }

            // Decode actual bitmap
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext Result.failure(Exception("Failed to decode image"))

            // Scale to max 512px
            val scaled = scaleBitmap(bitmap, maxDim)

            // Compress to JPEG 80%
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()

            // Recycle
            if (scaled !== bitmap) bitmap.recycle()
            scaled.recycle()

            // Upload to Firebase Storage
            val ref = storage.reference.child("profiles/$uid/avatar.jpg")
            ref.putBytes(data).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // Update Firestore user doc
            firestore.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Use the Google Sign-In photo URL directly (no upload needed).
     */
    suspend fun useGooglePhotoAsDefault(googlePhotoUrl: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not signed in"))

            firestore.collection("users").document(uid)
                .update("photoUrl", googlePhotoUrl)
                .await()

            Result.success(googlePhotoUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDim
            newHeight = (maxDim / ratio).toInt()
        } else {
            newHeight = maxDim
            newWidth = (maxDim * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
