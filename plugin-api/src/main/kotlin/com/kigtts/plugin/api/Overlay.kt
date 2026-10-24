package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Requires overlay capability. IDs, actions and bubbles are scoped to the calling publisher. */
@Serializable
data class OverlayEntrance(val id: String, val label: String, val symbol: String = "extension",
    val actionId: String? = null, val pageId: String? = null, val showLabel: Boolean = true,
    val category: String = "", val badge: PluginBadge? = null) {
    constructor(id: String, label: String, symbol: String = "extension", actionId: String? = null,
        pageId: String? = null, showLabel: Boolean = true) : this(id, label, symbol, actionId, pageId, showLabel, "", null)
    fun copy(id: String = this.id, label: String = this.label, symbol: String = this.symbol, actionId: String? = this.actionId,
        pageId: String? = this.pageId, showLabel: Boolean = this.showLabel): OverlayEntrance =
        OverlayEntrance(id, label, symbol, actionId, pageId, showLabel, category, badge)
}

/** A transient native bubble, never arbitrary HTML. Use clear-bubble to dismiss. */
@Serializable
data class OverlayBubble(val text: String, val actionLabel: String? = null, val symbol: String = "open_in_new",
    val actionId: String? = null, val pageId: String? = null, val durationMs: Long = 8000)
