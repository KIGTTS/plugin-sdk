package com.kigtts.plugin.api

import kotlin.test.*

class ResourceIconCompatibilityTest {
    @Test fun monochromeMetadataRoundTripsAndLegacyCopyKeepsIt() {
        val icon = PluginResource("icon.svg", monochrome = true)
        assertEquals(icon, PluginJson.decodeFromString<PluginResource>(PluginJson.encodeToString(icon)))
        assertTrue(icon.copy(path = "renamed.svg").monochrome)
        assertFalse(PluginJson.decodeFromString<PluginResource>("""{"path":"old.png"}""").monochrome)
    }
    @Test fun oldConstructorAndDefaultCopyBridgesStillExist() {
        val type = PluginResource::class.java
        assertTrue(type.constructors.any { it.parameterCount == 2 })
        assertTrue(type.constructors.any { it.parameterCount == 4 })
        assertTrue(type.declaredMethods.any { it.name == "copy" && it.parameterCount == 2 })
        assertTrue(type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == 5 })
    }
}
