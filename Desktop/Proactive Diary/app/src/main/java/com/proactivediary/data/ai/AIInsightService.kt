package com.proactivediary.data.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.proactivediary.data.db.dao.PreferenceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class AIInsightResult(
    val summary: String,
    val themes: List<String>,
    val moodTrend: String,
    val promptSuggestions: List<String>
)

@Singleton
class AIInsightService @Inject constructor(
    private val preferenceDao: PreferenceDao
) {
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getApiKey(): String? {
        val buildKey = com.proactivediary.BuildConfig.GEMINI_API_KEY
        return if (buildKey.isNotBlank()) buildKey else null
    }

    suspend fun isEnabled(): Boolean {
        return preferenceDao.getValue("ai_insights_enabled") == "true"
    }

    suspend fun generateInsight(entries: List<EntryForAI>): AIInsightResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext null
        if (entries.isEmpty()) return@withContext null

        val entriesText = entries.joinToString("\n\n---\n\n") { entry ->
            buildString {
                append("Date: ${entry.date}\n")
                if (entry.title.isNotBlank()) append("Title: ${entry.title}\n")
                append(entry.content)
            }
        }

        val systemPrompt = """You are a thoughtful journaling companion that analyzes diary entries.
Given a week's journal entries, provide:
1. A brief, warm summary of the week (2-3 sentences)
2. Key themes (3-5 single words or short phrases)
3. Mood trend: one of "improving", "stable", "declining", or "mixed"
4. 3 personalized writing prompts for the upcoming week based on what the user wrote about

Respond ONLY with valid JSON in this exact format:
{
  "summary": "...",
  "themes": ["...", "..."],
  "moodTrend": "...",
  "promptSuggestions": ["...", "...", "..."]
}

Be empathetic, supportive, and insightful. Never be judgmental."""

        val userMessage = "Here are my journal entries from this past week:\n\n$entriesText"

        try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "$systemPrompt\n\n$userMessage"))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 500,
                    responseMimeType = "application/json"
                )
            )

            val jsonBody = gson.toJson(requestBody)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .addHeader("content-type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBody = response.body?.string() ?: return@withContext null
            val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
            val text = geminiResponse.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text ?: return@withContext null

            // Parse the JSON response
            val insightJson = gson.fromJson(text, AIInsightJson::class.java)
            AIInsightResult(
                summary = insightJson.summary ?: "No summary available",
                themes = insightJson.themes ?: emptyList(),
                moodTrend = insightJson.moodTrend ?: "stable",
                promptSuggestions = insightJson.promptSuggestions ?: emptyList()
            )
        } catch (_: Exception) {
            null
        }
    }
}

data class EntryForAI(
    val date: String,
    val title: String,
    val content: String
)

// Gemini API request/response models
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

private data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

private data class GeminiPart(
    val text: String
)

private data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 500,
    @SerializedName("responseMimeType") val responseMimeType: String? = null
)

private data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

private data class GeminiCandidate(
    val content: GeminiContent?
)

private data class AIInsightJson(
    val summary: String?,
    val themes: List<String>?,
    val moodTrend: String?,
    val promptSuggestions: List<String>?
)
