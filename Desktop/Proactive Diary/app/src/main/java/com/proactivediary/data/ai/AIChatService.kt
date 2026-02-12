package com.proactivediary.data.ai

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.EntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared AI chat service used by:
 * - "Go Deeper" (per-entry reflection)
 * - "Talk to Your Journal" (full journal chat)
 *
 * Handles API calls, context building, and message management.
 */
@Singleton
class AIChatService @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val entryDao: EntryDao
) {
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getApiKey(): String? {
        return preferenceDao.getValue("ai_api_key")
    }

    /**
     * "Go Deeper" — Reflect on a single entry.
     * Returns AI follow-up response based on the entry content + context.
     */
    suspend fun goDeeper(
        entryId: String,
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext null
        val entry = entryDao.getByIdSync(entryId) ?: return@withContext null

        val entryContext = buildEntryContext(entry)

        val systemPrompt = """You are a thoughtful, empathetic journaling companion embedded in a personal diary app called Proactive Diary. The user just finished writing a diary entry and wants to reflect deeper.

Here is context about their entry:
$entryContext

Your role:
- Ask one insightful follow-up question that helps the user reflect deeper
- Draw from CBT (Cognitive Behavioral Therapy) and positive psychology principles
- Reference specific details from their entry (location, weather, mood, what they wrote)
- Be warm but not overly cheerful. Match their emotional tone.
- Keep responses to 2-3 sentences max. One question at the end.
- Never give advice unless asked. Just help them think.
- Use their name if mentioned in the entry. Otherwise, use "you."

Style: Gentle, literary, like a wise friend over tea. Not clinical."""

        val messages = buildConversationMessages(conversationHistory, userMessage)
        callClaude(apiKey, systemPrompt, messages)
    }

    /**
     * "Talk to Your Journal" — Chat with your full journal history.
     * The AI has read all entries and can answer questions about patterns, themes, etc.
     */
    suspend fun talkToJournal(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext null

        // Get recent entries for context (last 30 entries max to fit token limit)
        val allEntries = entryDao.getAllSync().take(30)
        if (allEntries.isEmpty()) return@withContext null

        val journalContext = buildJournalContext(allEntries)

        val systemPrompt = """You are a wise, perceptive journaling companion embedded in a personal diary app called Proactive Diary. You have read the user's journal entries and know their life intimately.

Here is a summary of their journal:
$journalContext

Your role:
- Answer questions about their journal, patterns, moods, and life themes
- Notice connections the user might not see ("You mention your sister when you're stressed")
- Reference specific entries, dates, locations, and moods naturally
- Help them see growth ("3 months ago you were anxious about X, but lately you write about it with confidence")
- Be honest but kind. If patterns are concerning, mention it gently.
- Keep responses to 3-4 sentences unless they ask for more detail
- When asked about emotions or patterns, back it up with evidence from their entries

Style: Like a therapist who has read your diary — insightful, non-judgmental, precise. Use their writing style and vocabulary when possible."""

        val messages = buildConversationMessages(conversationHistory, userMessage)
        callClaude(apiKey, systemPrompt, messages)
    }

    private fun buildEntryContext(entry: EntryEntity): String {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate()
        val text = entry.contentPlain ?: entry.content

        return buildString {
            appendLine("Date: ${date.format(dateFormatter)}")
            if (entry.title.isNotBlank()) appendLine("Title: ${entry.title}")
            entry.mood?.let { appendLine("Mood: $it") }
            entry.locationName?.let { appendLine("Location: $it") }
            entry.weatherCondition?.let { condition ->
                val temp = entry.weatherTemp?.let { "${it.toInt()}\u00B0" } ?: ""
                appendLine("Weather: $temp $condition")
            }
            appendLine("Word count: ${entry.wordCount}")
            appendLine()
            appendLine("Entry content:")
            appendLine(text.take(2000))
        }
    }

    private fun buildJournalContext(entries: List<EntryEntity>): String {
        val zone = ZoneId.systemDefault()

        // Summary stats
        val totalEntries = entries.size
        val totalWords = entries.sumOf { it.wordCount }
        val moods = entries.mapNotNull { it.mood }
        val moodCounts = moods.groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
        val locations = entries.mapNotNull { it.locationName }.distinct()

        val firstDate = entries.minByOrNull { it.createdAt }?.let {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate().format(dateFormatter)
        }
        val lastDate = entries.maxByOrNull { it.createdAt }?.let {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate().format(dateFormatter)
        }

        return buildString {
            appendLine("=== JOURNAL OVERVIEW ===")
            appendLine("Entries: $totalEntries | Words: $totalWords | Period: $firstDate to $lastDate")
            if (moodCounts.isNotEmpty()) {
                appendLine("Moods: ${moodCounts.joinToString(", ") { "${it.key} (${it.value}x)" }}")
            }
            if (locations.isNotEmpty()) {
                appendLine("Common locations: ${locations.take(5).joinToString(", ")}")
            }
            appendLine()
            appendLine("=== RECENT ENTRIES (newest first) ===")

            for (entry in entries.take(20)) {
                val date = Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate()
                val text = (entry.contentPlain ?: entry.content).take(300)
                appendLine()
                appendLine("--- ${date.format(dateFormatter)} ---")
                if (entry.title.isNotBlank()) appendLine("Title: ${entry.title}")
                entry.mood?.let { appendLine("Mood: $it") }
                entry.locationName?.let { appendLine("Location: $it") }
                appendLine(text)
                if ((entry.contentPlain ?: entry.content).length > 300) appendLine("[...]")
            }
        }
    }

    private fun buildConversationMessages(
        history: List<ChatMessage>,
        currentMessage: String
    ): List<ClaudeChatMessage> {
        val messages = mutableListOf<ClaudeChatMessage>()

        for (msg in history) {
            messages.add(ClaudeChatMessage(
                role = if (msg.isUser) "user" else "assistant",
                content = msg.text
            ))
        }

        messages.add(ClaudeChatMessage(role = "user", content = currentMessage))
        return messages
    }

    private fun callClaude(
        apiKey: String,
        systemPrompt: String,
        messages: List<ClaudeChatMessage>
    ): String? {
        return try {
            val requestBody = ClaudeChatRequest(
                model = "claude-sonnet-4-5-20250929",
                maxTokens = 400,
                system = systemPrompt,
                messages = messages
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
            if (!response.isSuccessful) return null

            val responseBody = response.body?.string() ?: return null
            val claudeResponse = gson.fromJson(responseBody, ClaudeChatResponse::class.java)
            claudeResponse.content?.firstOrNull()?.text
        } catch (_: Exception) {
            null
        }
    }
}

// Chat message for conversation history
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Claude API models for chat
private data class ClaudeChatRequest(
    val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeChatMessage>
)

private data class ClaudeChatMessage(
    val role: String,
    val content: String
)

private data class ClaudeChatResponse(
    val content: List<ClaudeChatContent>?
)

private data class ClaudeChatContent(
    val text: String?
)
