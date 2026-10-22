package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Requires overlay capability. IDs, actions and bubbles are scoped to the calling publisher. */
@Serializable
data class OverlayEntrance(val id: String, val label: String, val symbol: String = "extension",
    val actionId: String? = null, val pageId: String? = null, val showLabel: Boolean = true)

/** A transient native bubble, never arbitrary HTML. Use clear-bubble to dismiss. */
@Serializable
data class OverlayBubble(val text: String, val actionLabel: String? = null, val symbol: String = "open_in_new",
    val actionId: String? = null, val pageId: String? = null, val durationMs: Long = 8000)
