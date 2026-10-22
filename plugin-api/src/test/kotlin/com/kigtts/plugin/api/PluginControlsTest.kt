package com.kigtts.plugin.api

import kotlin.test.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class PluginControlsTest {
    @Test fun settingsRoundTripAcrossWorkerWithRealControlTypes() {
        val request: PluginOutcome = PluginOutcome.Controls("LED 样式", listOf(
            PluginControl.Color("字幕色", "color", 0xFFFFFFFF),
            PluginControl.Slider("密度", "density", 24f, 8f, 256f, 247, " 行"),
            PluginControl.Toggle("发光", "glow", true),
            PluginControl.Choice("灯珠", "shape", listOf(PluginControlOption("round", "圆形"),
                PluginControlOption("square", "方形")), "round"),
            PluginControl.Choice("预设", "preset", listOf(PluginControlOption("classic", "经典")),
                "classic", PluginChoiceStyle.SELECT)), "reset")
        assertEquals(request, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(request)))
    }
}
