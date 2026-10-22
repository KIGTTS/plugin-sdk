package com.kigtts.plugin.api

import org.junit.Test
import org.junit.Assert.*

class VoiceParameterCompatibilityTest {
    @Test fun oldPackConstructorsDefaultMaskCopyAndSerializedCatalogStayCompatible() {
        val type = VoicePackInfo::class.java
        assertTrue(type.constructors.any { it.parameterCount == 7 })
        assertTrue(type.constructors.any { it.parameterCount == 9 })
        val oldJson = """{"id":"old","metadata":{"name":"旧包"},"voiceIds":["one"]}"""
        val old = PluginJson.decodeFromString<VoicePackInfo>(oldJson)
        assertTrue(old.parameters.isEmpty())
        val parameter = VoiceParameter("volume", "音量", 1f, 0f, 2f, 1f)
        val current = old.copy(parameters = listOf(parameter))
        assertEquals(listOf(parameter), current.copy(metadata = VoicePackMetadata("改名")).parameters)
        assertEquals(current, PluginJson.decodeFromString<VoicePackInfo>(PluginJson.encodeToString(current)))
    }
    @Test fun inlineParametersRoundTripAndValidateFiniteRanges() {
        val block: PageBlock = PageBlock.Slider(PluginControl.Slider("语速", "rate", 1f, .25f, 4f, suffix = "×"))
        assertEquals(block, PluginJson.decodeFromString<PageBlock>(PluginJson.encodeToString(block)))
        PluginSnapshot(PluginDescriptor("test.audio", "声音", ""), PluginContributions(), listOf(PluginPage("settings", "设置", listOf(block))))
        assertTrue(runCatching { VoiceParameter("gain", "音量", Float.NaN, 0f, 2f, 1f) }.isFailure)
        val bad = PageBlock.Slider(PluginControl.Slider("坏参数", "bad", 9f, 0f, 2f))
        assertTrue(runCatching { PluginSnapshot(PluginDescriptor("test.audio", "声音", ""), PluginContributions(),
            listOf(PluginPage("settings", "设置", listOf(bad)))) }.isFailure)
    }
}
