package com.kigtts.plugin.api

import kotlin.test.*

class OverlayContractTest {
    @Test fun registrationAndBubbleRoundTripWithoutIntroducingAnAutomaticApprovalField() {
        val commands = listOf<AppCommand>(AppCommand.Overlay("register", shortcut = OverlayEntrance("open", "打开", actionId = "show")),
            AppCommand.Overlay("request-add", shortcut = OverlayEntrance("page", "页面", pageId = "main")),
            AppCommand.Overlay("bubble", bubble = OverlayBubble("已经完成", "查看", pageId = "result")))
        for (command in commands) {
            val encoded = PluginJson.encodeToString(AppCommand.serializer(), command)
            assertEquals(command, PluginJson.decodeFromString(AppCommand.serializer(), encoded))
            assertFalse(encoded.contains("approved")); assertFalse(encoded.contains("publisher"))
        }
        assertEquals(22, PLUGIN_API_VERSION); assertEquals(9, MIN_PLUGIN_API_VERSION)
    }
}
