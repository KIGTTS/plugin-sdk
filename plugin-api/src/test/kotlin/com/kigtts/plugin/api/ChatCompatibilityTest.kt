package com.kigtts.plugin.api

import kotlin.test.*

class ChatCompatibilityTest {
    @Test fun preOptionsBinaryConstructorAndDefaultCopyRemainCallable() {
        val types = arrayOf(String::class.java, ChatRole::class.java, String::class.java, String::class.java,
            ChatMessageState::class.java, String::class.java, String::class.java, String::class.java,
            PluginIcon::class.java, List::class.java, Boolean::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!)
        val message = ChatMessage::class.java.getConstructor(*types).newInstance("old", ChatRole.ASSISTANT, "answer", "",
            ChatMessageState.COMPLETE, "", "", "", PluginIcon.INFO, emptyList<ChatImage>(), false, 3, true)
        assertTrue(message.hasReasoning)
        assertTrue(message.options.isEmpty())
        assertNotNull(ChatMessage::class.java.constructors.firstOrNull { it.parameterCount == 15 && it.parameterTypes.last().name.endsWith("DefaultConstructorMarker") })
        val oldCopy = ChatMessage::class.java.declaredMethods.single { it.name == "copy\$default" && it.parameterCount == 16 }
        val copied = oldCopy.invoke(null, message, null, null, "changed", null, null, null, null, null, null, null, false, 0, false,
            8191 xor 4, null) as ChatMessage
        assertEquals("changed", copied.text)
        assertTrue(copied.hasReasoning)
    }
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
