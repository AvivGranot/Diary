package com.proactivediary.ui.chat

import com.proactivediary.data.ai.ChatMessage
import org.junit.Assert.*
import org.junit.Test

class GoDeeperViewModelTest {

    // ─── GoDeeperUiState tests ───

    @Test
    fun `default GoDeeperUiState has expected values`() {
        val state = GoDeeperUiState()
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isApiKeyMissing)
        assertEquals("", state.entryId)
        assertNull(state.error)
    }

    @Test
    fun `GoDeeperUiState copy preserves values`() {
        val messages = listOf(
            ChatMessage(text = "Hello", isUser = false),
            ChatMessage(text = "Hi there", isUser = true)
        )
        val state = GoDeeperUiState(
            messages = messages,
            isLoading = true,
            isApiKeyMissing = false,
            entryId = "entry-123",
            error = null
        )
        assertEquals(2, state.messages.size)
        assertTrue(state.isLoading)
        assertFalse(state.isApiKeyMissing)
        assertEquals("entry-123", state.entryId)
        assertNull(state.error)
    }

    @Test
    fun `GoDeeperUiState with error stores message`() {
        val state = GoDeeperUiState(error = "Something went wrong. Try again.")
        assertNotNull(state.error)
        assertEquals("Something went wrong. Try again.", state.error)
    }

    @Test
    fun `GoDeeperUiState api key missing flag`() {
        val state = GoDeeperUiState(isApiKeyMissing = true)
        assertTrue(state.isApiKeyMissing)
    }

    // ─── ChatMessage tests ───

    @Test
    fun `ChatMessage user message`() {
        val msg = ChatMessage(text = "How am I feeling?", isUser = true)
        assertEquals("How am I feeling?", msg.text)
        assertTrue(msg.isUser)
    }

    @Test
    fun `ChatMessage AI message`() {
        val msg = ChatMessage(text = "You seem reflective today.", isUser = false)
        assertEquals("You seem reflective today.", msg.text)
        assertFalse(msg.isUser)
    }
}
