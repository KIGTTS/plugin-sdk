package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Host-owned controls and exterior. ARGB colors only; text/layout/safe areas remain intact. */
@Serializable
data class PanelPresentation(val fullscreenBackground: Long? = null, val fadeControlsWhenIdle: Boolean = false,
    /** Reserve horizontal gestures inside the subtitle surface, without disabling the rest of Home. */
    val blockHomeSideSwipes: Boolean = false,
    val homeChrome: HomeChromePresentation = HomeChromePresentation()) {
    // Keep the API 26–37 Kotlin constructor/default mask and copy/default entry points.
    constructor(fullscreenBackground: Long? = null, fadeControlsWhenIdle: Boolean = false, blockHomeSideSwipes: Boolean = false)
        : this(fullscreenBackground, fadeControlsWhenIdle, blockHomeSideSwipes, HomeChromePresentation())
    constructor(fullscreenBackground: Long? = null, fadeControlsWhenIdle: Boolean = false)
        : this(fullscreenBackground, fadeControlsWhenIdle, false)
    fun copy(fullscreenBackground: Long? = this.fullscreenBackground, fadeControlsWhenIdle: Boolean = this.fadeControlsWhenIdle,
        blockHomeSideSwipes: Boolean = this.blockHomeSideSwipes): PanelPresentation =
        PanelPresentation(fullscreenBackground, fadeControlsWhenIdle, blockHomeSideSwipes, homeChrome)
    fun copy(fullscreenBackground: Long? = this.fullscreenBackground, fadeControlsWhenIdle: Boolean = this.fadeControlsWhenIdle): PanelPresentation =
        PanelPresentation(fullscreenBackground, fadeControlsWhenIdle, blockHomeSideSwipes, homeChrome)
    init { require(fullscreenBackground == null || fullscreenBackground in 0L..0xffffffffL) }
}

/** Fade the host toolbar and quick-text dock independently without changing their bounds. */
@Serializable
data class HomeChromePresentation(
    val fadeHeaderWhenIdle: Boolean = false,
    val fadeDockWhenIdle: Boolean = false,
)
