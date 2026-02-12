package com.proactivediary.data.weather

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherData(
    val temperature: Double,   // Celsius
    val condition: String,     // "Sunny", "Cloudy", etc.
    val icon: String           // WMO code string for display
)

@Singleton
class WeatherService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // Simple cache: lat/lng rounded to 2 decimals + timestamp
    private var cachedWeather: WeatherData? = null
    private var cacheKey: String = ""
    private var cacheTimestamp: Long = 0
    private val cacheDurationMs = 30 * 60 * 1000L // 30 minutes

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherData? {
        val key = "%.2f,%.2f".format(latitude, longitude)
        val now = System.currentTimeMillis()

        if (key == cacheKey && cachedWeather != null && (now - cacheTimestamp) < cacheDurationMs) {
            return cachedWeather
        }

        return try {
            val result = fetchWeather(latitude, longitude)
            if (result != null) {
                cachedWeather = result
                cacheKey = key
                cacheTimestamp = now
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchWeather(lat: Double, lng: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current_weather=true&temperature_unit=celsius"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val apiResponse = gson.fromJson(body, OpenMeteoResponse::class.java)
            val current = apiResponse.currentWeather ?: return@withContext null

            WeatherData(
                temperature = current.temperature,
                condition = wmoToCondition(current.weathercode),
                icon = current.weathercode.toString()
            )
        }
    }

    private fun wmoToCondition(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1 -> "Mostly Clear"
            2 -> "Partly Cloudy"
            3 -> "Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow Grains"
            80, 81, 82 -> "Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Hail Storm"
            else -> "Unknown"
        }
    }

    // Open-Meteo API response models
    private data class OpenMeteoResponse(
        @SerializedName("current_weather") val currentWeather: CurrentWeather?
    )

    private data class CurrentWeather(
        val temperature: Double,
        val weathercode: Int
    )
}
