package com.kigtts.plugin.api

import kotlin.test.*

class OverlayContractTest {
    @Test fun entranceCategoriesAndBadgesKeepOldBinaryConstructorsAndCopies() {
        val entry = OverlayEntrance("agent", "AI助手", actionId = "open", category = "助手", badge = PluginBadge(tone = BadgeTone.CAUTION))
        val command = AppCommand.Overlay("register", shortcut = entry)
        assertEquals(command, PluginJson.decodeFromString(AppCommand.serializer(), PluginJson.encodeToString(AppCommand.serializer(), command)))
        assertEquals(entry.badge, entry.copy(label = "助手").badge)
        assertEquals("助手", entry.copy(label = "助手").category)
        val constructor = OverlayEntrance::class.java.constructors.single { it.parameterTypes.size == 6 }
        val old = constructor.newInstance("legacy", "旧入口", "extension", "open", null, true) as OverlayEntrance
        assertNull(old.badge)
        val defaults = OverlayEntrance::class.java.constructors.single { it.parameterTypes.size == 8 && it.parameterTypes.last().simpleName == "DefaultConstructorMarker" }
        assertEquals("extension", (defaults.newInstance("legacy", "旧入口", null, null, null, false, 60, null) as OverlayEntrance).symbol)
    }
    @Test fun registrationAndBubbleRoundTripWithoutIntroducingAnAutomaticApprovalField() {
        val commands = listOf<AppCommand>(AppCommand.Overlay("register", shortcut = OverlayEntrance("open", "打开", actionId = "show")),
            AppCommand.Overlay("request-add", shortcut = OverlayEntrance("page", "页面", pageId = "main")),
            AppCommand.Overlay("bubble", bubble = OverlayBubble("已经完成", "查看", pageId = "result")))
        for (command in commands) {
            val encoded = PluginJson.encodeToString(AppCommand.serializer(), command)
            assertEquals(command, PluginJson.decodeFromString(AppCommand.serializer(), encoded))
            assertFalse(encoded.contains("approved")); assertFalse(encoded.contains("publisher"))
        }
        assertTrue(PLUGIN_API_VERSION >= 22); assertEquals(9, MIN_PLUGIN_API_VERSION)
    }
}
