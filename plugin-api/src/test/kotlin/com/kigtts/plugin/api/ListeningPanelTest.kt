package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.*

class ListeningPanelTest {
    @Test fun secondaryPanelsRoundTripWithoutChangingOldCommands() {
        listOf<PluginOutcome>(PluginOutcome.SetListeningPanel("tools"),
            PluginOutcome.SetListeningPanel("overlay", false, false), PluginOutcome.SetListeningPanel(),
            PluginOutcome.HostAction(HostCommand.OPEN_LISTENING_PANEL)).forEach {
            assertEquals(it, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(it)))
        }
        assertEquals(5, HostCommand.TOGGLE_SUBTITLE_FULLSCREEN.ordinal)
    }
}
