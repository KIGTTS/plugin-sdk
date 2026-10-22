package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Resources remain in the presenting plugin's data directory; HTML callbacks are explicitly declared. */
@Serializable
sealed interface SubtitleContent {
    @Serializable data class Text(val text: String) : SubtitleContent
    @Serializable data class Image(val source: PluginResource, val description: String = "", val motionVideo: PluginResource? = null, val appleLive: Boolean = false) : SubtitleContent
    @Serializable data class Video(val source: PluginResource, val description: String = "", val mimeType: String = "video/mp4") : SubtitleContent
    /** Inline UTF-8 <=128 KiB; use an owned resource for larger pages (<=2 MiB). */
    @Serializable data class Html(val source: PluginResource? = null, val html: String = "", val description: String = "网页",
        val allowedActions: List<String> = emptyList(), val allowNetwork: Boolean = false) : SubtitleContent
    @Serializable data class Web(val url: String, val description: String = "网页") : SubtitleContent
}
