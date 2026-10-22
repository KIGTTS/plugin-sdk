package com.kigtts.plugin.api

import java.nio.file.Files
import kotlin.test.*

class PluginStorageTest {
    @Test fun rejectsEscapesAndKeepsAllDirectoriesUnderPluginRoot() {
        val root = Files.createTempDirectory("plugin-data").toFile()
        try {
            val storage = PluginStorage(root)
            for (path in listOf("../outside", "/outside", "models/../../outside", "models\\outside", "models//bad", ""))
                assertFailsWith<IllegalArgumentException>(path) { storage.file(path) }
            for (directory in listOf(storage.models, storage.downloads, storage.temporary, storage.configuration)) {
                assertTrue(directory.canonicalFile.toPath().startsWith(root.canonicalFile.toPath()))
            }
        } finally { root.deleteRecursively() }
    }
    @Test fun apiRoundTripsDeclarativeUiRequestsAndStreamingCapability() {
        val request: PluginOutcome = PluginOutcome.ImportResource("模型", "models", "imported", zip = true)
        assertEquals(request, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(request)))
        val snapshot = PluginSnapshot(PluginDescriptor("test.engine", "测试", "test"), PluginContributions(), emptyList(),
            asr = AsrInfo(streaming = StreamingKind.SEGMENTED, availability = EngineAvailability.NEEDS_RESOURCES))
        assertEquals(snapshot, PluginJson.decodeFromString<PluginSnapshot>(PluginJson.encodeToString(snapshot)))
        assertFailsWith<IllegalArgumentException> { TtsRequest("text", rate = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { AsrInfo(batch = false, streaming = StreamingKind.NONE) }
    }
    @Test fun oversizedAndDeepUiContributionsAreRejectedBeforeHostRendering() {
        val descriptor = PluginDescriptor("test.ui", "UI", "test")
        assertFailsWith<IllegalArgumentException> {
            PluginSnapshot(descriptor, PluginContributions(actions = List(501) { UiAction("$it", "按钮", UiSlot.TAB_END) }), emptyList())
        }
        var nested: PageBlock = PageBlock.Text("content")
        repeat(10) { nested = PageBlock.Group("group", listOf(nested)) }
        assertFailsWith<IllegalArgumentException> {
            PluginSnapshot(descriptor, PluginContributions(), listOf(PluginPage("page", "page", listOf(nested))))
        }
    }
}
