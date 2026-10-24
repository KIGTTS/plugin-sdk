package com.kigtts.plugin.api

import kotlin.test.*

class StrokeGuideTest {
    private val guide = PageBlock.StrokeGuide("Z", "从左上起笔", listOf(StrokePoint(.1f, .2f), StrokePoint(.9f, .2f), StrokePoint(.1f, .8f)),
        toggle = ManagementToggle(true, "toggle"), actions = listOf(PageAction("choose", "选择快捷文本")))
    private fun snapshot(block: PageBlock) = PluginSnapshot(PluginDescriptor("test.guide", "手势", "test"),
        PluginContributions(), listOf(PluginPage("guide", "手势", listOf(block))))
    @Test fun nativeGuideRoundTripsAcrossWorkerSerialization() {
        val original = snapshot(guide)
        assertEquals(original, PluginJson.decodeFromString<PluginSnapshot>(PluginJson.encodeToString(original)))
    }
    @Test fun invalidOrUnboundedCoordinatesAreRejectedBeforeRendering() {
        listOf(listOf(StrokePoint(Float.NaN, .2f), StrokePoint(.3f, .4f)),
            listOf(StrokePoint(-.1f, .2f), StrokePoint(.3f, .4f)),
            List(129) { StrokePoint(it / 128f, .2f) }, List(3) { StrokePoint(.2f, .2f) }).forEach {
            assertFailsWith<IllegalArgumentException> { snapshot(guide.copy(points = it)) }
        }
    }
}
