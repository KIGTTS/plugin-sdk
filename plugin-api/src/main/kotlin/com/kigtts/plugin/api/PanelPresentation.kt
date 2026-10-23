package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Host-owned controls and exterior. ARGB colors only; text/layout/safe areas remain intact. */
@Serializable
data class PanelPresentation(val fullscreenBackground: Long? = null, val fadeControlsWhenIdle: Boolean = false) {
    init { require(fullscreenBackground == null || fullscreenBackground in 0L..0xffffffffL) }
}
