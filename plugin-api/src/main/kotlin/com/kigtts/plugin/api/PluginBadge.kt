package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable
enum class BadgeTone { THEME, WARNING, CAUTION }

/** Null on an entry hides the badge; a null count shows a dot. Hosts display counts above 99 as 99+. */
@Serializable
data class PluginBadge(val count: Int? = null, val description: String = "有新内容", val tone: BadgeTone = BadgeTone.THEME) {
    init {
        require(count == null || count in 1..999_999)
        require(description.isNotBlank() && description.length <= 160)
    }
}
