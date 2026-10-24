package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** A scoped resource reference. An explicit owner allows a service to show provider metadata. */
@Serializable
data class PluginResource(val path: String, val pluginId: String? = null)

/** Rendered by the host; plugins supply data and commands, never Compose or their own styles. */
@Serializable
data class ManagementToggle(val checked: Boolean, val action: String)
/** A radio control separate from opening a row or invoking its secondary actions. */
@Serializable
data class ManagementSelection(val action: String, val enabled: Boolean = true)
@Serializable
data class ManagementItem(val id: String, val title: String, val subtitle: String = "",
    val icon: PluginIcon = PluginIcon.INFO, val image: PluginResource? = null,
    val selected: Boolean = false, val pinned: Boolean = false, val group: String = "",
    val primaryAction: String? = null, val actions: List<PageAction> = emptyList(), val toggle: ManagementToggle? = null,
    val symbol: String? = null, val badges: List<String> = emptyList(), val selection: ManagementSelection? = null) {
    // Keep the API14–23 constructor/copy (including Kotlin default masks) callable by signed packages.
    constructor(id: String, title: String, subtitle: String = "", icon: PluginIcon = PluginIcon.INFO, image: PluginResource? = null,
        selected: Boolean = false, pinned: Boolean = false, group: String = "", primaryAction: String? = null,
        actions: List<PageAction> = emptyList(), toggle: ManagementToggle? = null, symbol: String? = null)
        : this(id, title, subtitle, icon, image, selected, pinned, group, primaryAction, actions, toggle, symbol, emptyList(), null)
    // Preserve API9–13 JVM constructors and copy masks for already signed packages.
    constructor(id: String, title: String, subtitle: String = "", icon: PluginIcon = PluginIcon.INFO, image: PluginResource? = null,
        selected: Boolean = false, pinned: Boolean = false, group: String = "", primaryAction: String? = null,
        actions: List<PageAction> = emptyList(), toggle: ManagementToggle? = null)
        : this(id, title, subtitle, icon, image, selected, pinned, group, primaryAction, actions, toggle, null)
    fun copy(id: String = this.id, title: String = this.title, subtitle: String = this.subtitle, icon: PluginIcon = this.icon,
        image: PluginResource? = this.image, selected: Boolean = this.selected, pinned: Boolean = this.pinned, group: String = this.group,
        primaryAction: String? = this.primaryAction, actions: List<PageAction> = this.actions, toggle: ManagementToggle? = this.toggle): ManagementItem =
        ManagementItem(id, title, subtitle, icon, image, selected, pinned, group, primaryAction, actions, toggle, symbol, badges, selection)
    fun copy(id: String = this.id, title: String = this.title, subtitle: String = this.subtitle, icon: PluginIcon = this.icon,
        image: PluginResource? = this.image, selected: Boolean = this.selected, pinned: Boolean = this.pinned, group: String = this.group,
        primaryAction: String? = this.primaryAction, actions: List<PageAction> = this.actions, toggle: ManagementToggle? = this.toggle,
        symbol: String? = this.symbol): ManagementItem =
        ManagementItem(id, title, subtitle, icon, image, selected, pinned, group, primaryAction, actions, toggle, symbol, badges, selection)
    init { require(badges.size <= 4 && badges.all { it.isNotBlank() && it.length <= 32 }) }
}

@Serializable
data class ManagementFilter(val id: String, val label: String, val action: String, val selected: Boolean = false,
    val symbol: String? = null) {
    // Preserve the four-argument constructor used by existing signed plugins.
    constructor(id: String, label: String, action: String, selected: Boolean)
        : this(id, label, action, selected, null)
}
/** A host-themed inline action. A progress value renders a cancellable ring; null is indeterminate. */
@Serializable
data class ManagementProgressAction(val label: String, val action: String? = null,
    val progress: Float? = null, val running: Boolean = false) {
    init { require(label.length in 1..80 && (action == null || action.length in 1..240))
        require(progress == null || progress.isFinite() && progress in 0f..1f) }
}
@Serializable enum class ManagementListStyle { STANDARD, CATALOG }
/** Change the action token after appending rows; the host requests each token once near the end. */
@Serializable data class ManagementLoadMore(val action: String) {
    init { require(action.length in 1..240) }
}
@Serializable
data class ManagementList(val id: String, val items: List<ManagementItem>,
    val reorderAction: String? = null, val emptyText: String = "暂无条目", val filters: List<ManagementFilter> = emptyList(),
    val style: ManagementListStyle = ManagementListStyle.STANDARD,
    val itemActions: Map<String, ManagementProgressAction> = emptyMap(),
    val shortcuts: List<PageAction> = emptyList(), val loadMore: ManagementLoadMore? = null) {
    constructor(id: String, items: List<ManagementItem>, reorderAction: String? = null, emptyText: String = "暂无条目",
        filters: List<ManagementFilter> = emptyList(), style: ManagementListStyle = ManagementListStyle.STANDARD,
        itemActions: Map<String, ManagementProgressAction> = emptyMap())
        : this(id, items, reorderAction, emptyText, filters, style, itemActions, emptyList(), null)
    constructor(id: String, items: List<ManagementItem>, reorderAction: String? = null, emptyText: String = "暂无条目",
        filters: List<ManagementFilter> = emptyList(), style: ManagementListStyle = ManagementListStyle.STANDARD)
        : this(id, items, reorderAction, emptyText, filters, style, emptyMap())
    constructor(id: String, items: List<ManagementItem>, reorderAction: String? = null, emptyText: String = "暂无条目", filters: List<ManagementFilter> = emptyList())
        : this(id, items, reorderAction, emptyText, filters, ManagementListStyle.STANDARD)
    fun copy(id: String = this.id, items: List<ManagementItem> = this.items, reorderAction: String? = this.reorderAction,
        emptyText: String = this.emptyText, filters: List<ManagementFilter> = this.filters): ManagementList = ManagementList(id, items, reorderAction, emptyText, filters, style, itemActions, shortcuts, loadMore)
    fun copy(id: String = this.id, items: List<ManagementItem> = this.items, reorderAction: String? = this.reorderAction,
        emptyText: String = this.emptyText, filters: List<ManagementFilter> = this.filters,
        style: ManagementListStyle = this.style): ManagementList = ManagementList(id, items, reorderAction, emptyText, filters, style, itemActions, shortcuts, loadMore)
    fun copy(id: String = this.id, items: List<ManagementItem> = this.items, reorderAction: String? = this.reorderAction,
        emptyText: String = this.emptyText, filters: List<ManagementFilter> = this.filters,
        style: ManagementListStyle = this.style, itemActions: Map<String, ManagementProgressAction> = this.itemActions): ManagementList =
        ManagementList(id, items, reorderAction, emptyText, filters, style, itemActions, shortcuts, loadMore)
    init { require(items.size <= 1000 && items.map { it.id }.distinct().size == items.size)
        require(shortcuts.size <= 12 && shortcuts.map { it.id }.distinct().size == shortcuts.size)
        require(itemActions.size <= 1000 && itemActions.keys.all { key -> items.any { it.id == key } }) }
}

/** Keeps installed/new entries deterministic while retaining a saved order for returning IDs. */
object ItemOrder {
    fun reconcile(saved: List<String>, current: List<String>): List<String> =
        saved.filter { it in current }.distinct() + current.filter { it !in saved }.distinct()
    fun move(order: List<String>, from: String, to: String): List<String> {
        val result = order.toMutableList(); val source = result.indexOf(from); val target = result.indexOf(to)
        if (source < 0 || target < 0 || source == target) return order
        result.add(target, result.removeAt(source)); return result
    }
    fun validate(order: List<String>, current: List<String>) {
        require(order.size == current.size && order.distinct().size == order.size && order.toSet() == current.toSet()) { "列表内容已变化，请重新排序" }
    }
}
