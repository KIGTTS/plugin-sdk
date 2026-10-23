package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*

class PanelPresentationTest {
    @Test fun presentationRoundTripsAndOldCopiesKeepOptions() {
        val renderer = PluginOutcome.SetSubtitleRenderer(PluginResource("led"), "index.html", PanelPresentation(0xff000000L, true))
        val listening = PluginOutcome.SetListeningPanel("led", true, true, true)
        listOf<PluginOutcome>(renderer, listening).forEach {
            assertEquals(it, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(it)))
        }
        assertEquals(renderer.presentation, renderer.copy(mainPage = "other.html").presentation)
        assertTrue(listening.copy(pageId = "other").fadeControlsWhenIdle)
        assertNotNull(PluginOutcome.SetSubtitleRenderer::class.java.getConstructor(PluginResource::class.java, String::class.java))
        assertNotNull(PluginOutcome.SetListeningPanel::class.java.getConstructor(String::class.java, Boolean::class.javaPrimitiveType, Boolean::class.javaPrimitiveType))
    }
    @Test fun oldRequestsDoNotChangePresentation() {
        assertEquals(PanelPresentation(), PluginOutcome.SetSubtitleRenderer().presentation)
        assertFalse(PluginOutcome.SetListeningPanel("tools").fadeControlsWhenIdle)
        assertFailsWith<IllegalArgumentException> { PanelPresentation(-1) }
        assertFailsWith<IllegalArgumentException> { PanelPresentation(0x100000000L) }
    }
}
