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
}
