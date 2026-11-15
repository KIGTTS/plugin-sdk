package com.kigtts.plugin.api

import org.junit.Assert.*
import org.junit.Test

class TtsProfileCompatibilityTest {
    @Test fun oldJsonDefaultsToRealtimeAndOlderEnginesHaveNoProfileSupport() {
        assertEquals(TtsProfile.REALTIME, PluginJson.decodeFromString<TtsRequest>("""{"text":"hi"}""").profile)
        assertNull(PluginJson.decodeFromString<TtsInfo>("{}").profiles)
    }

    @Test fun preservesProfileThroughSegmentationAndCatalogSerialization() {
        val request = TtsRequest("first", profile = TtsProfile.PREBAKE)
        val segmented = request.copy(text = "second", voiceId = "v")
        assertEquals(TtsProfile.PREBAKE, PluginJson.decodeFromString<TtsRequest>(PluginJson.encodeToString(segmented)).profile)
        val profiles = TtsProfileVersions("2", "4")
        val info = TtsInfo(profiles = profiles).copy(maxTextLength = 500)
        assertEquals(profiles, PluginJson.decodeFromString<TtsInfo>(PluginJson.encodeToString(info)).profiles)
    }

    @Test fun retainsOldConstructorAndDefaultCopyForInstalledDex() {
        val request = TtsRequest::class.java.getConstructor(String::class.java, String::class.java,
            Float::class.javaPrimitiveType, String::class.java).newInstance("text", "zh-CN", 1f, null)
        assertEquals(TtsProfile.REALTIME, request.profile)
        val copy = TtsRequest::class.java.getMethod("copy\$default", TtsRequest::class.java,
            String::class.java, String::class.java, Float::class.javaPrimitiveType, String::class.java,
            Int::class.javaPrimitiveType, Any::class.java)
        assertEquals(request, copy.invoke(null, request, null, null, 0f, null, 15, null))
        TtsInfo::class.java.getMethod("copy\$default", TtsInfo::class.java,
            EngineAvailability::class.java, List::class.java, Int::class.javaPrimitiveType, TtsRole::class.java,
            String::class.java, Boolean::class.javaPrimitiveType, Boolean::class.javaPrimitiveType, List::class.java,
            Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType, Any::class.java)
    }
}
