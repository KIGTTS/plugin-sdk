package com.kigtts.plugin.api

import kotlin.test.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class SubtitleContentTest {
    @Test fun sessionRendererCanEnableAndDisableAcrossWorkerProtocol() {
        val renderers = listOf(PluginOutcome.SetSubtitleRenderer(PluginResource("renderer", "display.led")),
            PluginOutcome.SetSubtitleRenderer())
        renderers.forEach { renderer ->
            val wire = PluginJson.encodeToString<PluginOutcome>(renderer)
            assertEquals(renderer, PluginJson.decodeFromString<PluginOutcome>(wire))
        }
        assertEquals(9, MIN_PLUGIN_API_VERSION)
        assertEquals(18, PLUGIN_API_VERSION)
    }
    @Test fun foregroundGestureCommandsRoundTripAndOldEnumsRemainAvailable() {
        val results = listOf(PluginOutcome.SendQuickText("existing-entry"), PluginOutcome.NoAction,
            PluginOutcome.HostAction(HostCommand.TOGGLE_SUBTITLE_FULLSCREEN))
        results.forEach { assertEquals(it, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(it))) }
        val gesture = GestureAction("stroke", "手势触发", PluginGesture.SUBTITLE_STROKE)
        assertEquals(gesture, PluginJson.decodeFromString<GestureAction>(PluginJson.encodeToString(gesture)))
        assertEquals(0, PluginGesture.QUICK_TEXT_LONG_PRESS.ordinal)
    }
    @Test fun allMediaVariantsRoundTripOverWorkerProtocolWithoutAndroidViews() {
        val content = listOf(SubtitleContent.Text("文字"), SubtitleContent.Image(PluginResource("resources/image.jpg")),
            SubtitleContent.Image(PluginResource("still.heic"), motionVideo = PluginResource("clip.mov"), appleLive = true),
            SubtitleContent.Video(PluginResource("resources/video.mp4")),
            SubtitleContent.Html(html = "<h1>网页</h1>", allowedActions = listOf("refresh")),
            SubtitleContent.Html(source = PluginResource("resources/page/index.html")), SubtitleContent.Web("https://example.org"))
        content.forEach { media -> val outcome: PluginOutcome = PluginOutcome.PresentContent(media)
            assertEquals(outcome, PluginJson.decodeFromString<PluginOutcome>(PluginJson.encodeToString(outcome))) }
    }
    @Test fun plainContextDefaultsRemainTextAndMediaContextRoundTrips() {
        val old = PluginJson.decodeFromString<PluginContext>("{\"editorText\":\"旧文本\",\"transcript\":\"\"}")
        assertEquals("TEXT", old.contentKind); assertNull(old.contentId)
        val media = old.copy(contentKind = "IMAGE", contentId = "image-id")
        assertEquals(media, PluginJson.decodeFromString<PluginContext>(PluginJson.encodeToString(media)))
    }
}
