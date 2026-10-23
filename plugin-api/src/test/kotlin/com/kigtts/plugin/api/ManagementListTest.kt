package com.kigtts.plugin.api

import org.junit.Assert.*
import org.junit.Test

class ManagementListTest {
    @Test fun radioAndBadgesRoundTripWithoutBreakingOldItemConstructorsOrCopies() {
        val item = ManagementItem("voice", "流萤", primaryAction = "details", selected = true,
            badges = listOf("Piper"), selection = ManagementSelection("use"))
        assertEquals(item, PluginJson.decodeFromString<ManagementItem>(PluginJson.encodeToString(item)))
        assertEquals(item.selection, item.copy(title = "声音").selection)
        assertEquals(item.badges, item.copy(symbol = "voice_chat").badges)
        for (count in listOf(11, 12)) {
            val type = ManagementItem::class.java
            assertTrue(type.constructors.any { it.parameterCount == count })
            assertTrue(type.declaredMethods.any { it.name == "copy" && it.parameterCount == count })
            assertTrue(type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == count + 3 })
        }
        val old = PluginJson.decodeFromString<ManagementItem>("""{"id":"old","title":"旧包"}""")
        assertTrue(old.badges.isEmpty()); assertNull(old.selection)
    }
    @Test fun itemOrderKeepsSavedPositionsAndAppendsNewIds() {
        assertEquals(listOf("b", "a", "c"), ItemOrder.reconcile(listOf("b", "gone", "a"), listOf("a", "b", "c")))
        assertEquals(listOf("b", "c", "a"), ItemOrder.move(listOf("a", "b", "c"), "a", "c"))
        assertThrows(IllegalArgumentException::class.java) { ItemOrder.validate(listOf("a", "a"), listOf("a", "b")) }
    }
    @Test fun managementPageAndScopedImagesRoundTripAcrossWire() {
        val page = PluginPage("items", "条目", emptyList(), ManagementList("test", listOf(ManagementItem("one", "条目", image = PluginResource("images/avatar.png"),
            pinned = true, primaryAction = "open", toggle = ManagementToggle(true, "toggle"))), "reorder"))
        assertEquals(page, PluginJson.decodeFromString<PluginPage>(PluginJson.encodeToString(page)))
    }
    @Test fun inlineActionsRoundTripAndOldCopyRetainsThem() {
        for (count in listOf(5, 6)) {
            val type = ManagementList::class.java
            assertTrue(type.constructors.any { it.parameterCount == count })
            assertTrue(type.declaredMethods.any { it.name == "copy" && it.parameterCount == count })
            assertTrue(type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == count + 3 })
        }
        val list = ManagementList("store", listOf(ManagementItem("one", "Plugin")),
            itemActions = mapOf("one" to ManagementProgressAction("取消下载", "cancel/one", .5f, true)))
        assertEquals(list, PluginJson.decodeFromString<ManagementList>(PluginJson.encodeToString(list)))
        assertEquals(list.itemActions, list.copy(emptyText = "Empty").itemActions)
        assertThrows(IllegalArgumentException::class.java) { ManagementProgressAction("Bad", progress = Float.NaN) }
        assertThrows(IllegalArgumentException::class.java) { ManagementProgressAction("Bad", progress = 2f) }
    }
}
