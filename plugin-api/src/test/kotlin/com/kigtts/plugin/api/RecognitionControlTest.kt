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
    @Test fun packProviderLabelSurvivesLegacyCopyAndSerialization() {
        val pack = VoicePackInfo("pack", VoicePackMetadata("声音"), listOf("voice"), providerLabel = "腾讯云")
        assertEquals("腾讯云", pack.copy(metadata = VoicePackMetadata("新名称")).providerLabel)
        assertEquals("腾讯云", pack.copy(parameters = emptyList()).providerLabel)
        assertEquals(pack, PluginJson.decodeFromString<VoicePackInfo>(PluginJson.encodeToString(pack)))
    }
}
