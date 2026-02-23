package com.proactivediary.data.ai

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.proactivediary.BuildConfig
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.domain.model.StoryAspectRatio
import com.proactivediary.domain.model.StoryStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class GrokImageResult {
    data class Success(val imageFile: File) : GrokImageResult()
    data class Error(val message: String) : GrokImageResult()
}

@Singleton
class GrokImageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val isApiAvailable: Boolean
        get() = BuildConfig.GROK_API_KEY.isNotBlank()

    fun buildPrompt(entry: EntryEntity, style: StoryStyle): String {
        val parts = mutableListOf<String>()

        // Title
        if (entry.title.isNotBlank()) {
            parts.add("Scene inspired by: \"${entry.title}\"")
        }

        // Content excerpt (first 200 chars of plain text)
        val plainText = entry.contentPlain ?: entry.content
        if (plainText.isNotBlank()) {
            val excerpt = if (plainText.length > 200) plainText.take(200).trimEnd() + "..." else plainText
            parts.add("Story context: $excerpt")
        }

        // Location
        entry.locationName?.let { parts.add("Setting: $it") }

        // Weather
        if (entry.weatherCondition != null && entry.weatherTemp != null) {
            parts.add("Weather: ${entry.weatherCondition}, ${entry.weatherTemp.toInt()}°")
        }

        // Time of day from createdAt
        val hour = Instant.ofEpochMilli(entry.createdAt)
            .atZone(ZoneId.systemDefault())
            .hour
        val timeOfDay = when (hour) {
            in 5..6 -> "dawn"
            in 7..10 -> "morning"
            in 11..13 -> "midday"
            in 14..16 -> "afternoon"
            in 17..19 -> "sunset"
            else -> "night"
        }
        parts.add("Time of day: $timeOfDay")

        // Style suffix
        parts.add(style.promptSuffix)

        // Hard instruction
        parts.add("No text, no letters, no words in the image")

        return parts.joinToString(". ")
    }

    suspend fun generateImage(
        entry: EntryEntity,
        style: StoryStyle,
        aspectRatio: StoryAspectRatio
    ): GrokImageResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROK_API_KEY
        if (apiKey.isBlank()) {
            return@withContext GrokImageResult.Error("API key not configured")
        }

        val prompt = buildPrompt(entry, style)

        val requestBody = GrokGenerateRequest(
            model = "grok-2-image",
            prompt = prompt,
            n = 1,
            response_format = "url",
            size = aspectRatio.apiSize
        )

        val jsonBody = gson.toJson(requestBody)

        try {
            val request = Request.Builder()
                .url("https://api.x.ai/v1/images/generations")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorMsg = when (response.code) {
                    401 -> "Invalid API key"
                    429 -> "Too many requests — wait a moment"
                    else -> "Generation failed (${response.code})"
                }
                return@withContext GrokImageResult.Error(errorMsg)
            }

            val responseBody = response.body?.string()
                ?: return@withContext GrokImageResult.Error("Empty response")

            val grokResponse = gson.fromJson(responseBody, GrokGenerateResponse::class.java)
            val imageUrl = grokResponse.data?.firstOrNull()?.url
                ?: return@withContext GrokImageResult.Error("Generation failed — try again")

            // Download the image
            val imageRequest = Request.Builder().url(imageUrl).build()
            val imageResponse = client.newCall(imageRequest).execute()
            if (!imageResponse.isSuccessful) {
                return@withContext GrokImageResult.Error("Failed to download image")
            }

            val cacheDir = File(context.cacheDir, "story_images")
            cacheDir.mkdirs()
            val fileName = "story_${System.currentTimeMillis()}.png"
            val file = File(cacheDir, fileName)

            imageResponse.body?.byteStream()?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext GrokImageResult.Error("Failed to save image")

            cleanCache()

            GrokImageResult.Success(file)
        } catch (e: java.net.SocketTimeoutException) {
            GrokImageResult.Error("Network timeout — try again")
        } catch (e: java.net.UnknownHostException) {
            GrokImageResult.Error("No internet connection")
        } catch (_: Exception) {
            GrokImageResult.Error("Network error — try again")
        }
    }

    private fun cleanCache() {
        val cacheDir = File(context.cacheDir, "story_images")
        if (!cacheDir.exists()) return
        val files = cacheDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        if (files.size > 10) {
            files.drop(10).forEach { it.delete() }
        }
    }
}

private data class GrokGenerateRequest(
    val model: String,
    val prompt: String,
    val n: Int,
    val response_format: String,
    val size: String
)

private data class GrokGenerateResponse(
    val data: List<GrokImageData>?
)

private data class GrokImageData(
    val url: String?
)
