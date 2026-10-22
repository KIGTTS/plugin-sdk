package com.kigtts.plugin.api

import kotlin.test.*

class ChatCompatibilityTest {
    @Test fun apiNineChatConstructorsAndCopiesPreserveNewMetadata() {
        assertNotNull(ChatMessage::class.java.constructors.firstOrNull { it.parameterCount == 5 })
        assertNotNull(ChatMessage::class.java.constructors.firstOrNull { it.parameterCount == 9 })
        assertNotNull(PageBlock.Chat::class.java.constructors.firstOrNull { it.parameterCount == 9 })
        assertNotNull(ChatMessage::class.java.declaredMethods.firstOrNull { it.name == "copy\$default" && it.parameterCount == 12 })
        assertNotNull(PageBlock.Chat::class.java.constructors.firstOrNull { it.parameterCount == 5 })
        assertNotNull(ChatMessage::class.java.declaredMethods.firstOrNull { it.name == "copy\$default" && it.parameterCount == 8 })
        assertNotNull(PageBlock.Chat::class.java.declaredMethods.firstOrNull { it.name == "copy\$default" && it.parameterCount == 8 })
        val message = ChatMessage("id", ChatRole.ASSISTANT, "one", summary = "summary", toolName = "file")
        assertEquals("summary", message.copy(text = "two").summary)
        val chat = PageBlock.Chat(listOf(message), "send", "stop", conversationId = "conversation", backgroundRunning = true)
        val updated = chat.copy(messages = emptyList())
        assertEquals("conversation", updated.conversationId); assertTrue(updated.backgroundRunning)
    }
    @Test fun currentHostAcceptsPreviousApiServices() {
        val plugin = object : KigPlugin {
            override val descriptor = PluginDescriptor("test.legacy", "legacy", "test", apiVersion = 9)
            override suspend fun execute(actionId: String, context: PluginContext) = PluginOutcome.RefreshUi
        }
        assertEquals(plugin, PluginRegistry(listOf(plugin)).plugin("test.legacy"))
    }
    @Test fun firstApiElevenConstructorsAndCopiesKeepCompletedThoughtFlag() {
        assertNotNull(ChatMessage::class.java.constructors.firstOrNull { it.parameterCount == 12 })
        assertNotNull(ChatMessage::class.java.declaredMethods.firstOrNull { it.name == "copy\$default" && it.parameterCount == 15 })
        val message = ChatMessage("done", ChatRole.ASSISTANT, "answer", hasReasoning = true)
        assertTrue(message.copy(text = "updated").hasReasoning)
        assertTrue(message.copy(position = 9).hasReasoning)
        assertNotNull(ChatQueuedMessage::class.java.constructors.firstOrNull { it.parameterCount == 4 })
        assertNotNull(ChatQueuedMessage::class.java.declaredMethods.firstOrNull { it.name == "copy\$default" && it.parameterCount == 7 })
        val queued = ChatQueuedMessage("id", "text", "remove", "front", "steer", "order")
        assertEquals("steer", queued.copy(text = "updated").steerAction)
    }
}
