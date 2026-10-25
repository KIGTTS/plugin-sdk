package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlin.test.*
import org.junit.Test

class OpenWebContractTest {
    @Test fun defaultsAndExplicitModesRoundTrip() {
        val command: AppCommand = AppCommand.OpenWeb("https://lhtstudio.com", "官网", "quick-text", true)
        assertEquals(command, PluginJson.decodeFromString<AppCommand>(PluginJson.encodeToString(command)))
        val defaults = PluginJson.decodeFromString<AppCommand>("""{"type":"open-web","url":"https://lhtstudio.com"}""") as AppCommand.OpenWeb
        assertEquals("custom-tab", defaults.mode); assertFalse(defaults.addToQuickText)
    }
}
