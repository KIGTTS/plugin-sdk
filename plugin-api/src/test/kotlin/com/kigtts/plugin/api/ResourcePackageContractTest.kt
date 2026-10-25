package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlin.test.*
import org.junit.Test

class ResourcePackageContractTest {
    @Test fun commandsAndDataRoundTrip() {
        val request: AppCommand = AppCommand.ResourcePackage(source = PluginResource("downloads/test.kigresource"), expectedPublisher = "a".repeat(64))
        assertEquals(request, PluginJson.decodeFromString<AppCommand>(PluginJson.encodeToString(request)))
        val content = ResourcePackageContent(frames = listOf(PolaroidFrameStyle("sample.frame", "纸白")))
        assertEquals(content, PluginJson.decodeFromString<ResourcePackageContent>(PluginJson.encodeToString(content)))
    }
}
