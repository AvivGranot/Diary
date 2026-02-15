package com.proactivediary.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ImageMetadata(
    val id: String,
    val filename: String,
    val width: Int,
    val height: Int,
    val addedAt: Long
) {
    val isPortrait: Boolean get() = height > width
}

@Singleton
class ImageStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File
        get() = File(context.filesDir, "images").also { it.mkdirs() }

    private fun entryDir(entryId: String): File =
        File(imagesDir, entryId).also { it.mkdirs() }

    private fun thumbDir(entryId: String): File =
        File(entryDir(entryId), "thumbs").also { it.mkdirs() }

    /**
     * Saves an image from a content URI, compressing to max 1920px and 80% JPEG quality.
     * Also generates a 200px thumbnail.
     * Returns the ImageMetadata for the saved image.
     */
    suspend fun saveImage(entryId: String, uri: Uri): ImageMetadata = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val filename = "img_$id.jpg"

        // Decode with inJustDecodeBounds to get original dimensions
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        val origWidth = options.outWidth
        val origHeight = options.outHeight

        // Calculate sample size for max 1920px
        val maxDim = 1920
        var sampleSize = 1
        if (origWidth > maxDim || origHeight > maxDim) {
            val halfWidth = origWidth / 2
            val halfHeight = origHeight / 2
            while ((halfWidth / sampleSize) >= maxDim || (halfHeight / sampleSize) >= maxDim) {
                sampleSize *= 2
            }
        }

        // Decode the actual bitmap
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: throw IllegalStateException("Failed to decode image from URI")

        // Scale to max 1920px if still too large
        val scaledBitmap = scaleBitmap(bitmap, maxDim)
        val finalWidth = scaledBitmap.width
        val finalHeight = scaledBitmap.height

        // Save full-size image
        val imageFile = File(entryDir(entryId), filename)
        FileOutputStream(imageFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        // Generate and save thumbnail (200px max dimension)
        val thumb = scaleBitmap(scaledBitmap, 200)
        val thumbFile = File(thumbDir(entryId), filename)
        FileOutputStream(thumbFile).use { out ->
            thumb.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }

        // Recycle bitmaps to free memory
        if (scaledBitmap !== bitmap) bitmap.recycle()
        thumb.recycle()
        scaledBitmap.recycle()

        ImageMetadata(
            id = id,
            filename = filename,
            width = finalWidth,
            height = finalHeight,
            addedAt = System.currentTimeMillis()
        )
    }

    /** Returns the File path for a full-size image. */
    fun getImageFile(entryId: String, filename: String): File =
        File(entryDir(entryId), filename)

    /** Returns the File path for a thumbnail image. */
    fun getThumbnailFile(entryId: String, filename: String): File =
        File(thumbDir(entryId), filename)

    /** Deletes a single image and its thumbnail. */
    suspend fun deleteImage(entryId: String, filename: String) = withContext(Dispatchers.IO) {
        File(entryDir(entryId), filename).delete()
        File(thumbDir(entryId), filename).delete()
    }

    /** Deletes all images for an entry. */
    suspend fun deleteAllImages(entryId: String) = withContext(Dispatchers.IO) {
        entryDir(entryId).deleteRecursively()
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
