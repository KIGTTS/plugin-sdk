package com.kigtts.plugin.api

import org.junit.Assert.*
import org.junit.Test

class ManagementListTest {
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
}
