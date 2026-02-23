package com.proactivediary.domain.suggestions

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.proactivediary.data.db.dao.ActivitySignalDao
import com.proactivediary.data.db.entities.ActivitySignalEntity
import com.proactivediary.data.db.dao.PreferenceDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SignalCollector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activitySignalDao: ActivitySignalDao,
    private val preferenceDao: PreferenceDao
) {
    companion object {
        private const val TAG = "SignalCollector"
        private const val PHOTO_LOOKBACK_MS = 4 * 60 * 60 * 1000L // 4 hours
    }

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Scans MediaStore for recent photos (last 4 hours) and creates
     * ActivitySignalEntity records with type="photo".
     */
    suspend fun collectPhotoSignals() {
        // Respect privacy preferences
        if (preferenceDao.getSync("privacy_photos")?.value == "false") {
            Log.d(TAG, "Photos disabled in privacy controls, skipping")
            return
        }

        if (!hasStoragePermission()) {
            Log.d(TAG, "No storage permission, skipping photo signals")
            return
        }

        val cutoff = System.currentTimeMillis() - PHOTO_LOOKBACK_MS
        val cutoffSeconds = cutoff / 1000

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        val selectionArgs = arrayOf(cutoffSeconds.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val signals = mutableListOf<ActivitySignalEntity>()

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idColumn)
                    val dateTaken = if (dateTakenColumn >= 0) cursor.getLong(dateTakenColumn) else 0L
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000 // seconds -> ms

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )

                    val timestamp = if (dateTaken > 0) dateTaken else dateAdded

                    val data = JSONObject().apply {
                        put("uri", contentUri.toString())
                        put("dateTaken", timestamp)
                    }.toString()

                    signals.add(
                        ActivitySignalEntity(
                            id = UUID.randomUUID().toString(),
                            type = "photo",
                            data = data,
                            timestamp = timestamp,
                            consumed = false,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            if (signals.isNotEmpty()) {
                activitySignalDao.insertAll(signals)
                Log.d(TAG, "Collected ${signals.size} photo signals")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to collect photo signals: ${e.message}")
        }
    }

    /**
     * Uses FusedLocationProviderClient for last known location,
     * creates a type="location" signal.
     */
    suspend fun collectLocationSignals() {
        // Respect privacy preferences
        if (preferenceDao.getSync("privacy_location")?.value == "false") {
            Log.d(TAG, "Location disabled in privacy controls, skipping")
            return
        }

        if (!hasLocationPermission()) {
            Log.d(TAG, "No location permission, skipping location signals")
            return
        }

        try {
            val location = getLastKnownLocation() ?: return

            val data = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("accuracy", location.accuracy)
            }.toString()

            val signal = ActivitySignalEntity(
                id = UUID.randomUUID().toString(),
                type = "location",
                data = data,
                timestamp = System.currentTimeMillis(),
                consumed = false,
                createdAt = System.currentTimeMillis()
            )

            activitySignalDao.insert(signal)
            Log.d(TAG, "Collected location signal: ${location.latitude}, ${location.longitude}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to collect location signal: ${e.message}")
        }
    }

    /**
     * Runs all signal collectors.
     */
    suspend fun collectAll() {
        collectPhotoSignals()
        collectLocationSignals()
    }

    @SuppressWarnings("MissingPermission")
    private suspend fun getLastKnownLocation(): android.location.Location? {
        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        // On API 33+ (TIRAMISU), use READ_MEDIA_IMAGES; below that, READ_EXTERNAL_STORAGE
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
