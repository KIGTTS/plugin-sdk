package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Host-owned controls and exterior. ARGB colors only; text/layout/safe areas remain intact. */
@Serializable
data class PanelPresentation(val fullscreenBackground: Long? = null, val fadeControlsWhenIdle: Boolean = false,
    /** Reserve horizontal gestures inside the subtitle surface, without disabling the rest of Home. */
    val blockHomeSideSwipes: Boolean = false) {
    constructor(fullscreenBackground: Long? = null, fadeControlsWhenIdle: Boolean = false)
        : this(fullscreenBackground, fadeControlsWhenIdle, false)
    fun copy(fullscreenBackground: Long? = this.fullscreenBackground, fadeControlsWhenIdle: Boolean = this.fadeControlsWhenIdle): PanelPresentation =
        PanelPresentation(fullscreenBackground, fadeControlsWhenIdle, blockHomeSideSwipes)
    init { require(fullscreenBackground == null || fullscreenBackground in 0L..0xffffffffL) }
}
