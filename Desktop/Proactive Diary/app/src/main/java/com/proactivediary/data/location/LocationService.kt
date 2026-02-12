package com.proactivediary.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val displayName: String
)

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getLocation(): LocationData? {
        if (!hasLocationPermission()) return null

        return try {
            val location = getCurrentLocation() ?: return null
            val displayName = reverseGeocode(location.latitude, location.longitude)
            LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                displayName = displayName ?: "%.4f, %.4f".format(location.latitude, location.longitude)
            )
        } catch (_: Exception) {
            null
        }
    }

    @SuppressWarnings("MissingPermission")
    private suspend fun getCurrentLocation(): android.location.Location? {
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

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                // Build a compact display name: "City, State" or "City, Country"
                val parts = listOfNotNull(
                    addr.locality ?: addr.subAdminArea,
                    addr.adminArea ?: addr.countryName
                )
                if (parts.isNotEmpty()) parts.joinToString(", ") else null
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
