package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Data-only native renderers. The host owns drawing, input, animation, and the window. */
@Serializable
sealed interface NativeSubtitleRenderer {
    @Serializable
    data class Led(
        val foreground: Long = 0xffffffffL,
        val background: Long = 0xff000000L,
        val dotMatrix: Boolean = true,
        val squareDots: Boolean = false,
        val dotRows: Int = 24,
        val dotSize: Float = .58f,
        val glow: Float = .42f,
        val displayHeight: Float = .72f,
        val fontScale: Float = 1f,
        val wrap: Boolean = false,
        val scrollSpeed: Float = 72f,
        val scrollRight: Boolean = false,
        val loopGap: Float = 96f,
        val alignment: LedAlignment = LedAlignment.CENTER,
        val quickSwipe: Boolean = true,
        val keepScreenOn: Boolean = true,
        val screenBrightness: Float? = 1f,
    ) : NativeSubtitleRenderer {
        init {
            require(foreground in 0..0xffffffffL && background in 0..0xffffffffL)
            require(dotRows in 8..256 && dotSize in .1f..1f && glow in 0f..1f)
            require(displayHeight in .35f..1f && fontScale in .5f..3f)
            require(scrollSpeed in 24f..1600f && loopGap in 24f..1600f)
            require(screenBrightness == null || screenBrightness in .1f..1f)
        }
    }
}

@Serializable
enum class LedAlignment { START, CENTER, END }
