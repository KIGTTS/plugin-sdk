package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import org.junit.Test
import org.junit.Assert.*

class ChatFileContractTest {
    @Test fun fileChatRoundTripsAndLegacyCopiesKeepFiles() {
        val file = ChatFile(PluginResource("imports/chat-files/曲子.mid"), "曲子.mid", 128, openAction = "save/1")
        val message = ChatMessage("m", ChatRole.USER, "", files = listOf(file))
        val chat = PageBlock.Chat(listOf(message), "send", "stop", fileAction = "attach-file", draftFiles = listOf(file), removeFileAction = "remove-file")
        assertEquals(chat, PluginJson.decodeFromString<PageBlock.Chat>(PluginJson.encodeToString(chat)))
        assertEquals(listOf(file), message.copy(text = "a").files)
        assertEquals(listOf(file), chat.copy(running = true).draftFiles)
        assertTrue(PluginJson.decodeFromString<ChatMessage>("""{"id":"old","role":"USER","text":"hi"}""").files.isEmpty())
        assertEquals("send", PageBlock.Chat(emptyList(), "send", "stop").sendAction)
    }
}
