package com.proactivediary.domain.recommendations

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.proactivediary.data.db.dao.EntryDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class DevicePhoto(
    val uri: Uri,
    val dateTaken: Long,
    val displayName: String
)

data class NearbyPlace(
    val id: String,
    val name: String,
    val types: List<String>,
    val lat: Double,
    val lng: Double
)

data class LocationSuggestion(
    val name: String,
    val lat: Double,
    val lng: Double,
    val lastVisited: Long
)

data class RecentEntry(
    val id: String,
    val title: String,
    val excerpt: String,
    @Deprecated("Mood feature removed") val mood: String? = null,
    val createdAt: Long
)

data class RecommendationsState(
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val devicePhotos: List<DevicePhoto> = emptyList(),
    val pastLocations: List<LocationSuggestion> = emptyList(),
    val recentEntries: List<RecentEntry> = emptyList(),
    val isLoading: Boolean = false,
    val hasPhotoPermission: Boolean = false
)

@Singleton
class RecommendationsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entryDao: EntryDao,
    private val placesClient: PlacesClient?
) {

    suspend fun getNearbyPlaces(lat: Double, lng: Double, radiusMeters: Double = 500.0): List<NearbyPlace> {
        val client = placesClient ?: return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val center = LatLng(lat, lng)
                val circle = CircularBounds.newInstance(center, radiusMeters)
                val placeFields = listOf(
                    Place.Field.ID,
                    Place.Field.DISPLAY_NAME,
                    Place.Field.TYPES,
                    Place.Field.LOCATION
                )
                val request = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(listOf("restaurant", "cafe", "park", "gym", "bar", "store", "library"))
                    .setMaxResultCount(10)
                    .build()

                val response = client.searchNearby(request).await()
                response.places.mapNotNull { place ->
                    val placeId = place.id ?: return@mapNotNull null
                    val name = place.displayName ?: return@mapNotNull null
                    val location = place.location ?: return@mapNotNull null
                    NearbyPlace(
                        id = placeId,
                        name = name,
                        types = place.placeTypes ?: emptyList(),
                        lat = location.latitude,
                        lng = location.longitude
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getRecentDevicePhotos(limit: Int = 20): List<DevicePhoto> {
        return withContext(Dispatchers.IO) {
            try {
                val photos = mutableListOf<DevicePhoto>()
                val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DISPLAY_NAME
                )

                val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
                val selectionArgs = arrayOf(sevenDaysAgo.toString())
                val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

                context.contentResolver.query(
                    collection, projection, selection, selectionArgs, sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                    var count = 0
                    while (cursor.moveToNext() && count < limit) {
                        val id = cursor.getLong(idCol)
                        val date = cursor.getLong(dateCol)
                        val name = cursor.getString(nameCol) ?: "Photo"
                        val uri = ContentUris.withAppendedId(collection, id)
                        photos.add(DevicePhoto(uri = uri, dateTaken = date, displayName = name))
                        count++
                    }
                }
                photos
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getRecentLocations(limit: Int = 10): List<LocationSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = entryDao.getAllSync()
                entries
                    .filter { it.locationName != null && it.latitude != null && it.longitude != null }
                    .groupBy { it.locationName!! }
                    .map { (name, group) ->
                        val latest = group.maxByOrNull { it.createdAt }!!
                        LocationSuggestion(
                            name = name,
                            lat = latest.latitude!!,
                            lng = latest.longitude!!,
                            lastVisited = latest.createdAt
                        )
                    }
                    .sortedByDescending { it.lastVisited }
                    .take(limit)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getRecentEntries(limit: Int = 5): List<RecentEntry> {
        return withContext(Dispatchers.IO) {
            try {
                entryDao.getPage(limit, 0).map { entry ->
                    val plain = entry.contentPlain ?: entry.content
                    RecentEntry(
                        id = entry.id,
                        title = entry.title.ifBlank { plain.take(40).trim() },
                        excerpt = plain.take(80).trim(),
                        mood = null,
                        createdAt = entry.createdAt
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
