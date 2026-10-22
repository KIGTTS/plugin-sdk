package com.kigtts.plugin.api

data class RegisteredAction(val plugin: KigPlugin, val action: UiAction) {
    val key: String get() = "${plugin.descriptor.id}/${action.id}"
}
data class RegisteredDrawerEntry(val plugin: KigPlugin, val entry: DrawerEntry)
data class RegisteredGesture(val plugin: KigPlugin, val action: GestureAction)

/** UI and engine catalog. External runtimes register proxies, never their implementation classes. */
class PluginRegistry(plugins: List<KigPlugin>) {
    @Volatile var plugins: List<KigPlugin> = emptyList()
        private set
    init { replace(plugins) }
    fun replace(next: List<KigPlugin>) {
        require(next.map { it.descriptor.id }.distinct().size == next.size) { "Duplicate plugin ID" }
        next.forEach { plugin ->
            require(plugin.descriptor.id.matches(Regex("[a-z][a-z0-9.-]+"))) { "Invalid plugin ID" }
            require(plugin.descriptor.apiVersion in MIN_PLUGIN_API_VERSION..PLUGIN_API_VERSION) { "Incompatible plugin API" }
            val actions = plugin.contributions.actions
            require(actions.map { it.id }.distinct().size == actions.size) { "Duplicate action ID" }
            require(actions.all { it.id.isNotBlank() && it.label.isNotBlank() })
            require(actions.all { (it.slot == UiSlot.EDITOR_MENU) == (it.presentation == UiPresentation.MENU_ITEM) }) {
                "Menu items must use EDITOR_MENU; other slots require buttons"
            }
            val entries = plugin.contributions.drawerEntries
            require(entries.map { it.id }.distinct().size == entries.size) { "Duplicate drawer entry ID" }
            require(entries.all { it.id.isNotBlank() && it.pageId.isNotBlank() && it.label.isNotBlank() })
            val gestures = plugin.contributions.gestures
            require((actions.map { it.id } + gestures.map { it.id }).distinct().size == actions.size + gestures.size) { "Duplicate gesture/action ID" }
            require(gestures.all { it.id.isNotBlank() && it.label.isNotBlank() })
        }
        plugins = next.toList()
    }

    fun missingRequirements(plugin: KigPlugin): List<PluginRequirement> = plugin.descriptor.requires.filter { requirement ->
        when (requirement) {
            PluginRequirement.RECOGNITION_SERVICE -> plugins.none { it.asr?.info?.role == AsrRole.SERVICE }
        }
    }
    fun available(plugin: KigPlugin) = missingRequirements(plugin).isEmpty()

    fun actions(slot: UiSlot, context: PluginContext = PluginContext("", "")): List<RegisteredAction> = plugins.filter(::available).flatMap { plugin ->
        plugin.contributions(context).actions.filter { it.slot == slot }.map { RegisteredAction(plugin, it) }
    }.sortedWith(compareBy({ it.action.order }, { it.key }))

    fun drawerEntries(): List<RegisteredDrawerEntry> = plugins.filter(::available).flatMap { plugin ->
        plugin.contributions.drawerEntries.map { RegisteredDrawerEntry(plugin, it) }
    }
    fun plugin(id: String): KigPlugin? = plugins.firstOrNull { it.descriptor.id == id }
    fun gestureActions(gesture: PluginGesture, context: PluginContext): List<RegisteredGesture> = plugins.filter(::available).flatMap { plugin ->
        plugin.contributions(context).gestures.filter { it.gesture == gesture }.map { RegisteredGesture(plugin, it) }
    }
}
