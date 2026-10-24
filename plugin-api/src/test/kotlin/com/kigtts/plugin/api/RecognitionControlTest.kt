package com.kigtts.plugin.api

import kotlin.test.*

class RecognitionControlTest {
    @Test fun optionalControlRoundTripsAndOldTtsProvidersKeepTheirDefaults() {
        val command = AsrControl(AsrControlOp.RELEASE)
        assertEquals(command, PluginJson.decodeFromString<AsrControl>(PluginJson.encodeToString(command)))
        val old = PluginJson.decodeFromString<TtsProvider>("""{"pluginId":"test.tts","name":"声音","info":{}}""")
        assertTrue(old.settingsPages.isEmpty())
        val provider = TtsProvider("test.tts", "声音", TtsInfo(), settingsPages = listOf(AsrSettingsPage("settings", "设置")))
        assertEquals(provider.settingsPages, provider.copy(enabled = false).settingsPages)
        assertEquals(provider, PluginJson.decodeFromString<TtsProvider>(PluginJson.encodeToString(provider)))
    }
}
