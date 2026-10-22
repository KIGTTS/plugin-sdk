package com.kigtts.plugin.api

import kotlin.test.*

class PagePresentationTest {
    @Test fun presentationRoundTripsAndOldCopyKeepsSymbol() {
        val item = ManagementItem("test", "插件", symbol = "extension")
        assertEquals("extension", item.copy(title = "新名称").symbol)
        val page = PluginPage("detail", "详情", listOf(
            PageBlock.Markdown("# 介绍\n\n![预览](asset:screen)", mapOf("asset:screen" to PluginResource("catalog-art/screen.img"))),
            PageBlock.Gallery(listOf(PageImage(PluginResource("catalog-art/screen.img"), "实际预览")))), ManagementList("items", listOf(item)))
        val snapshot = PluginSnapshot(PluginDescriptor("test.store", "商店", "test"), PluginContributions(), listOf(page))
        assertEquals(snapshot, PluginJson.decodeFromString<PluginSnapshot>(PluginJson.encodeToString(snapshot)))
        val oversized = page.copy(blocks = listOf(PageBlock.Markdown("x".repeat(32001))))
        assertFailsWith<IllegalArgumentException> { PluginSnapshot(snapshot.descriptor, snapshot.contributions, listOf(oversized)) }
    }
}
