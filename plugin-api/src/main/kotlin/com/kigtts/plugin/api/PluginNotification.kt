package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable enum class PluginNotificationKind { MESSAGE, PROGRESS, SHORTCUT }
/** target: plugin, home, quick-text, cards, sketch; actionId/pageId belong to the caller only. */
@Serializable data class PluginNotificationAction(val label: String, val target: String = "plugin",
    val pageId: String? = null, val actionId: String? = null, val phraseId: String? = null)

/** Reposting an id updates that notification. Complete tasks with ongoing=false/live=false.
 * live requests system promotion for active progress; OEM/user settings decide final presentation.
 */
@Serializable data class PluginNotification(val id: String, val title: String, val text: String = "",
    val kind: PluginNotificationKind = PluginNotificationKind.MESSAGE,
    val ongoing: Boolean = false, val live: Boolean = false,
    val progress: Int? = null, val progressMax: Int = 100, val indeterminate: Boolean = false,
    val symbol: String? = null, val contentAction: PluginNotificationAction? = null,
    val actions: List<PluginNotificationAction> = emptyList())
