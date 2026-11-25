package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*

class PanelPresentationTest {
    @Test fun sideSwipeReservationRoundTripsAndKeepsOldBinaryEntryPoints() {
        val value = PanelPresentation(blockHomeSideSwipes = true)
        assertEquals(value, PluginJson.decodeFromString<PanelPresentation>(PluginJson.encodeToString(value)))
        assertTrue(value.copy(fadeControlsWhenIdle = true).blockHomeSideSwipes)
        val old = PanelPresentation::class.java.getConstructor(java.lang.Long::class.java, Boolean::class.javaPrimitiveType)
        assertFalse((old.newInstance(null, false) as PanelPresentation).blockHomeSideSwipes)
        val defaults = PanelPresentation::class.java.constructors.single {
            it.parameterCount == 4 && it.parameterTypes.last().name.endsWith("DefaultConstructorMarker")
        }
        assertFalse((defaults.newInstance(null, false, 3, null) as PanelPresentation).blockHomeSideSwipes)
    }
    @Test fun homeChromeRoundTripsAndApi37DefaultsStayCallable() {
        val chrome = HomeChromePresentation(fadeHeaderWhenIdle = true, fadeDockWhenIdle = true)
        val value = PanelPresentation(homeChrome = chrome)
        assertEquals(chrome, value.copy(blockHomeSideSwipes = true).homeChrome)
        assertEquals(value, PluginJson.decodeFromString<PanelPresentation>(PluginJson.encodeToString(value)))
        val session = PluginOutcome.SetHomeChrome(chrome)
        assertEquals(session, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString<PluginOutcome>(session)))
        val constructor = PanelPresentation::class.java.constructors.single {
            it.parameterCount == 5 && it.parameterTypes.last().name.endsWith("DefaultConstructorMarker")
        }
        assertEquals(PanelPresentation(), constructor.newInstance(null, false, false, 7, null))
    }
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
