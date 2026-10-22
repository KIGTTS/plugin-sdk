package com.kigtts.plugin.api

import org.junit.Assert.*
import org.junit.Test

class SymbolCompatibilityTest {
    @Test fun previousNativeUiConstructorsCopiesAndDefaultBridgesRemainAvailable() {
        val entries = listOf(SettingsEntry::class.java to 8, PageAction::class.java to 3, PageBlock.Toggle::class.java to 5,
            PageBlock.Input::class.java to 6, PageBlock.Choice::class.java to 4, ManagedPackage::class.java to 4)
        entries.forEach { (type, size) ->
            assertTrue(type.name, type.constructors.any { it.parameterCount == size })
            assertTrue(type.name, type.declaredMethods.any { it.name == "copy" && it.parameterCount == size })
            assertTrue(type.name, type.declaredMethods.any { it.name == "copy\$default" && it.parameterCount == size + 3 })
        }
        assertEquals("calculate", SettingsEntry("id", "名称", "page", symbol = "calculate").copy(label = "修改").symbol)
        assertEquals("calculate", PageAction("id", "名称", symbol = "calculate").copy(label = "修改").symbol)
    }
}
