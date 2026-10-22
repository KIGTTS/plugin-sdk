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
        assertEquals(17, PLUGIN_API_VERSION)
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
