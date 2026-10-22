package com.kigtts.plugin.api

import org.junit.Assert.*
import org.junit.Test

class UiActionCompatibilityTest {
    @Test fun oldConstructorAndCopyRemainAvailableAndBusySurvivesLegacyCopy() {
        val types = arrayOf(String::class.java, String::class.java, UiSlot::class.java, PluginIcon::class.java,
            UiPresentation::class.java, Int::class.javaPrimitiveType, UiButtonStyle::class.java)
        assertNotNull(UiAction::class.java.getDeclaredConstructor(*types))
        assertNotNull(UiAction::class.java.getDeclaredMethod("copy", *types))
        val action = UiAction("open", "助手", UiSlot.HEADER_END, PluginIcon.ROBOT2, busy = true)
        assertTrue(action.copy(label = "Assistant").busy)
        assertEquals(action, PluginJson.decodeFromString<UiAction>(PluginJson.encodeToString(UiAction.serializer(), action)))
    }
}
