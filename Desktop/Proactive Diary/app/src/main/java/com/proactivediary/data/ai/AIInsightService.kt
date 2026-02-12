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
        return preferenceDao.getValue("ai_api_key")
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
            val requestBody = ClaudeRequest(
                model = "claude-sonnet-4-5-20250929",
                maxTokens = 500,
                system = systemPrompt,
                messages = listOf(
                    ClaudeMessage(role = "user", content = userMessage)
                )
            )

            val jsonBody = gson.toJson(requestBody)
            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val responseBody = response.body?.string() ?: return@withContext null
            val claudeResponse = gson.fromJson(responseBody, ClaudeResponse::class.java)
            val text = claudeResponse.content?.firstOrNull()?.text ?: return@withContext null

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

// Claude API request/response models
private data class ClaudeRequest(
    val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>
)

private data class ClaudeMessage(
    val role: String,
    val content: String
)

private data class ClaudeResponse(
    val content: List<ClaudeContent>?
)

private data class ClaudeContent(
    val text: String?
)

private data class AIInsightJson(
    val summary: String?,
    val themes: List<String>?,
    val moodTrend: String?,
    val promptSuggestions: List<String>?
)
