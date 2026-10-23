package com.kigtts.plugin.api

import kotlin.test.*

class PluginPageCompatibilityTest {
    @Test fun legacyConstructorsAndCopyBridgesRemainAvailable() {
        for ((type, count) in listOf(PluginPage::class.java to 4, PluginPage::class.java to 6, PluginPage::class.java to 8, PageBlock.Group::class.java to 2)) {
            assertTrue(type.constructors.any { it.parameterCount == count })
            assertTrue(type.constructors.any { it.parameterCount == count + 2 })
            assertTrue(type.declaredMethods.any { it.name == "copy" && it.parameterCount == count })
            assertTrue(type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == count + 3 })
        }
    }
    @Test fun optionalMetadataRoundTripsAndLegacyCopiesRetainIt() {
        val group = PageBlock.Group("权限说明", listOf(PageBlock.Text("访问网络")), collapsible = true)
        val page = PluginPage("store", "商店", listOf(group), header = ManagementItem("store", "商店", symbol = "storefront"), onEnterAction = "enter", loading = true, refreshAction = "refresh",
            headerAction = ManagementProgressAction("获取", "download"), search = PluginSearch("", "search"))
        assertTrue(group.copy(title = "权限").collapsible)
        assertEquals(page.header, page.copy(title = "详情").header)
        assertEquals("enter", page.copy(blocks = emptyList()).onEnterAction)
        assertTrue(page.copy(title = "副本").loading)
        assertEquals("refresh", page.copy(onEnterAction = "next").refreshAction)
        assertEquals(page.headerAction, page.copy(title = "详情").headerAction)
        assertEquals(page.search, page.copy(loading = false).search)
        val snapshot = PluginSnapshot(PluginDescriptor("test.store", "商店", ""), PluginContributions(), listOf(page))
        assertEquals(snapshot, PluginJson.decodeFromString<PluginSnapshot>(PluginJson.encodeToString(snapshot)))
        val old = PluginJson.decodeFromString<PluginPage>("""{"id":"old","title":"旧页","blocks":[]}""")
        assertNull(old.header); assertNull(old.onEnterAction)
        assertFalse(old.loading); assertNull(old.refreshAction)
        assertNull(old.headerAction); assertNull(old.search)
        assertFailsWith<IllegalArgumentException> {
            PluginSnapshot(snapshot.descriptor, snapshot.contributions, listOf(page.copy(onEnterAction = "x".repeat(129))))
        }
        assertFailsWith<IllegalArgumentException> {
            PluginSnapshot(snapshot.descriptor, snapshot.contributions, listOf(page.copy(refreshAction = "")))
        }
    }
}
