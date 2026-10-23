package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class PluginBadgeTest {
    @Test fun badgesSurviveSerializationAndLegacyCopies() {
        val badge = PluginBadge(120, "120 个插件可更新", BadgeTone.CAUTION)
        val action = UiAction("open", "打开", UiSlot.HEADER_END, badge = badge)
        val page = PageAction("open", "打开", badge = badge)
        val settings = SettingsEntry("settings", "设置", "page", badge = badge)
        assertEquals(badge, PluginJson.decodeFromString<UiAction>(PluginJson.encodeToString(action)).copy(label = "新的名称").badge)
        assertEquals(badge, PluginJson.decodeFromString<PageAction>(PluginJson.encodeToString(page)).copy(label = "新的名称").badge)
        assertEquals(badge, PluginJson.decodeFromString<SettingsEntry>(PluginJson.encodeToString(settings)).copy(label = "新的名称").badge)
        assertNull(PluginJson.decodeFromString<PageAction>("""{"id":"open","label":"打开"}""").badge)
        listOf(UiAction::class.java to 9, PageAction::class.java to 4, SettingsEntry::class.java to 9).forEach { (type, count) ->
            assertTrue(type.name, type.constructors.any { it.parameterCount == count })
            assertTrue(type.name, type.constructors.any { it.parameterCount == count + 2 })
            assertTrue(type.name, type.declaredMethods.any { it.name == "copy" && it.parameterCount == count })
            assertTrue(type.name, type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == count + 3 })
        }
    }
    @Test fun invalidCountsAndDescriptionsCannotEnterSnapshots() {
        assertNull(PluginBadge().count)
        assertThrows(IllegalArgumentException::class.java) { PluginBadge(0) }
        assertThrows(IllegalArgumentException::class.java) { PluginBadge(-1) }
        assertThrows(IllegalArgumentException::class.java) { PluginBadge(description = " ") }
    }
}
