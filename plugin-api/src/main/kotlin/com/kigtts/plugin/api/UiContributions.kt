package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable
enum class UiSlot {
    HEADER_START, HEADER_END,
    EDITOR_TOP_START, EDITOR_TOP_END, EDITOR_MENU, EDITOR_BOTTOM_START, EDITOR_BOTTOM_END,
    LISTENING_BOTTOM_START, LISTENING_BOTTOM_END,
    QUICK_PANEL_START, QUICK_PANEL_END, TAB_START, TAB_END, VOICE_INPUT_RELEASE,
}

@Serializable
enum class UiPresentation { BUTTON, MENU_ITEM }
@Serializable
enum class UiButtonStyle { ICON, ICON_TEXT }
@Serializable
enum class PluginIcon { INFO, MIC, SPEAKER, ADD, MENU, SETTINGS, TEXT, STAR, HEARING, CLOSE, PIN, EDIT, IMAGE, EXPORT, NONE, ROBOT2 }

@Serializable
data class UiAction(
    val id: String,
    val label: String,
    val slot: UiSlot,
    val icon: PluginIcon = PluginIcon.INFO,
    val presentation: UiPresentation = UiPresentation.BUTTON,
    val order: Int = 0,
    val buttonStyle: UiButtonStyle = UiButtonStyle.ICON,
    val busy: Boolean = false,
    val symbol: String? = null,
    val badge: PluginBadge? = null,
) {
    constructor(id: String, label: String, slot: UiSlot, icon: PluginIcon = PluginIcon.INFO,
        presentation: UiPresentation = UiPresentation.BUTTON, order: Int = 0, buttonStyle: UiButtonStyle = UiButtonStyle.ICON,
        busy: Boolean = false, symbol: String? = null) : this(id, label, slot, icon, presentation, order, buttonStyle, busy, symbol, null)
    constructor(id: String, label: String, slot: UiSlot, icon: PluginIcon = PluginIcon.INFO,
        presentation: UiPresentation = UiPresentation.BUTTON, order: Int = 0, buttonStyle: UiButtonStyle = UiButtonStyle.ICON)
        : this(id, label, slot, icon, presentation, order, buttonStyle, false)
    fun copy(id: String = this.id, label: String = this.label, slot: UiSlot = this.slot,
        icon: PluginIcon = this.icon, presentation: UiPresentation = this.presentation, order: Int = this.order,
        buttonStyle: UiButtonStyle = this.buttonStyle): UiAction = UiAction(id, label, slot, icon, presentation, order, buttonStyle, busy, symbol, badge)
    fun copy(id: String = this.id, label: String = this.label, slot: UiSlot = this.slot,
        icon: PluginIcon = this.icon, presentation: UiPresentation = this.presentation, order: Int = this.order,
        buttonStyle: UiButtonStyle = this.buttonStyle, busy: Boolean = this.busy, symbol: String? = this.symbol): UiAction =
        UiAction(id, label, slot, icon, presentation, order, buttonStyle, busy, symbol, badge)
}

/** Configuration entries are routed through Settings > Plugins instead of the feature drawer. */
@Serializable
data class DrawerEntry(val id: String, val label: String, val pageId: String, val icon: PluginIcon,
    val settingsPage: Boolean = false)
/** A feature entry shares the native settings card. Reserved card IDs have host-owned titles and order. */
@Serializable
data class SettingsEntry(val id: String, val label: String, val pageId: String,
    val icon: PluginIcon = PluginIcon.SETTINGS, val cardId: String = "extensions",
    val cardTitle: String = "功能与扩展", val summary: String = "", val order: Int = 0, val symbol: String? = null,
    val badge: PluginBadge? = null) {
    constructor(id: String, label: String, pageId: String, icon: PluginIcon = PluginIcon.SETTINGS, cardId: String = "extensions", cardTitle: String = "功能与扩展", summary: String = "", order: Int = 0, symbol: String? = null)
        : this(id, label, pageId, icon, cardId, cardTitle, summary, order, symbol, null)
    constructor(id: String, label: String, pageId: String, icon: PluginIcon = PluginIcon.SETTINGS, cardId: String = "extensions", cardTitle: String = "功能与扩展", summary: String = "", order: Int = 0)
        : this(id, label, pageId, icon, cardId, cardTitle, summary, order, null)
    fun copy(id: String = this.id, label: String = this.label, pageId: String = this.pageId, icon: PluginIcon = this.icon, cardId: String = this.cardId, cardTitle: String = this.cardTitle, summary: String = this.summary, order: Int = this.order): SettingsEntry =
        SettingsEntry(id, label, pageId, icon, cardId, cardTitle, summary, order, symbol, badge)
    fun copy(id: String = this.id, label: String = this.label, pageId: String = this.pageId, icon: PluginIcon = this.icon, cardId: String = this.cardId, cardTitle: String = this.cardTitle, summary: String = this.summary, order: Int = this.order, symbol: String? = this.symbol): SettingsEntry =
        SettingsEntry(id, label, pageId, icon, cardId, cardTitle, summary, order, symbol, badge)
}
/** Source runtimes expose actual installed/approved projects to dependency resolution. Drafts are excluded. */
@Serializable
data class ManagedPackage(val id: String, val version: String, val kind: String = "lua", val enabled: Boolean = true,
    val name: String = id, val permissions: Set<String> = emptySet(), val permissionPurposes: Map<String, String> = emptyMap(),
    val manageAction: String? = null, val enableAction: String? = null, val removeAction: String? = null, val error: String = "") {
    constructor(id: String, version: String, kind: String = "lua", enabled: Boolean = true) : this(id, version, kind, enabled, id)
    fun copy(id: String = this.id, version: String = this.version, kind: String = this.kind, enabled: Boolean = this.enabled): ManagedPackage =
        ManagedPackage(id, version, kind, enabled, name, permissions, permissionPurposes, manageAction, enableAction, removeAction, error)
}
@Serializable
enum class PluginGesture {
    QUICK_TEXT_LONG_PRESS,
    /** Single-finger stroke on the inactive TEXT subtitle surface, before text editing.
     * inputs: gesture.points = [[x,y],...] JSON, gesture.durationMs, gesture.width, gesture.height.
     * Bounded to 512 local pixel points; coordinates already respect subtitle rotation.
     * The foreground host ignores taps, long presses, pinch and child-owned gestures.
     */
    SUBTITLE_STROKE,
}
@Serializable
data class GestureAction(val id: String, val label: String, val gesture: PluginGesture)
@Serializable
data class PluginContributions(
    val actions: List<UiAction> = emptyList(), val drawerEntries: List<DrawerEntry> = emptyList(),
    val gestures: List<GestureAction> = emptyList(),
    /** Only these explicit entry points can be called through AppBroker by another plugin. Review buttons stay private. */
    val automationActions: List<String> = emptyList(),
    val settingsEntries: List<SettingsEntry> = emptyList(),
    val managedPackages: List<ManagedPackage> = emptyList(),
    /** Source deep links are delivered as untrusted inputs["url"], never fetched or installed by the host. */
    val sourceAction: String? = null,
)

@Serializable
data class PageAction(val id: String, val label: String, val icon: PluginIcon = PluginIcon.INFO, val symbol: String? = null,
    val badge: PluginBadge? = null) {
    constructor(id: String, label: String, icon: PluginIcon = PluginIcon.INFO, symbol: String? = null) : this(id, label, icon, symbol, null)
    constructor(id: String, label: String, icon: PluginIcon = PluginIcon.INFO) : this(id, label, icon, null)
    fun copy(id: String = this.id, label: String = this.label, icon: PluginIcon = this.icon): PageAction = PageAction(id, label, icon, symbol, badge)
    fun copy(id: String = this.id, label: String = this.label, icon: PluginIcon = this.icon, symbol: String? = this.symbol): PageAction = PageAction(id, label, icon, symbol, badge)
}

/** A plugin can build a new page without depending on Compose or an Activity. */
@Serializable
sealed interface PageBlock {
    /** A native vector row; tapping opens instructions and a replay of this single stroke. */
    @Serializable
    data class StrokeGuide(val title: String, val instruction: String, val points: List<StrokePoint>,
        val summary: String = "", val toggle: ManagementToggle? = null,
        val actions: List<PageAction> = emptyList()) : PageBlock
    /** Inline numeric setting; host commits a decimal value when the drag finishes. */
    @Serializable
    data class Slider(val control: PluginControl.Slider) : PageBlock
    /** Full-page sandboxed WebView. Local root belongs to this plugin; remote pages have no native bridge. */
    @Serializable
    data class Browser(val root: PluginResource? = null, val mainPage: String = "index.html", val url: String? = null,
        val allowNetwork: Boolean = false, val allowedActions: List<String> = emptyList()) : PageBlock
    @Serializable
    data class SourceEditor(val id: String, val text: String, val saveAction: String,
        val language: String = "lua", val readOnly: Boolean = false) : PageBlock
    @Serializable
    data class Files(val title: String, val items: List<PluginFileItem>, val directory: String,
        val openAction: String, val operationAction: String, val importAction: String? = null,
        val createAction: String? = null, val parentAction: String? = null) : PageBlock
    @Serializable
    data class Toggle(val title: String, val checked: Boolean, val actionId: String,
        val summary: String = "", val icon: PluginIcon = PluginIcon.SETTINGS, val symbol: String? = null) : PageBlock {
        constructor(title: String, checked: Boolean, actionId: String, summary: String = "", icon: PluginIcon = PluginIcon.SETTINGS) : this(title, checked, actionId, summary, icon, null)
        fun copy(title: String = this.title, checked: Boolean = this.checked, actionId: String = this.actionId, summary: String = this.summary, icon: PluginIcon = this.icon): Toggle = Toggle(title, checked, actionId, summary, icon, symbol)
    }
    @Serializable
    data class Select(val title: String, val options: List<PageAction>, val selected: String, val icon: PluginIcon = PluginIcon.NONE, val symbol: String? = null) : PageBlock
    @Serializable
    data class Choice(val title: String, val options: List<PageAction>, val selected: String,
        val icon: PluginIcon = PluginIcon.SETTINGS, val symbol: String? = null) : PageBlock {
        constructor(title: String, options: List<PageAction>, selected: String, icon: PluginIcon = PluginIcon.SETTINGS) : this(title, options, selected, icon, null)
        fun copy(title: String = this.title, options: List<PageAction> = this.options, selected: String = this.selected, icon: PluginIcon = this.icon): Choice = Choice(title, options, selected, icon, symbol)
    }
    @Serializable
    data class Input(val title: String, val value: String, val actionId: String,
        val secret: Boolean = false, val multiline: Boolean = false, val icon: PluginIcon = PluginIcon.EDIT, val symbol: String? = null,
        val trailingAction: PageAction? = null) : PageBlock {
        constructor(title: String, value: String, actionId: String, secret: Boolean = false, multiline: Boolean = false, icon: PluginIcon = PluginIcon.EDIT) : this(title, value, actionId, secret, multiline, icon, null, null)
        fun copy(title: String = this.title, value: String = this.value, actionId: String = this.actionId, secret: Boolean = this.secret, multiline: Boolean = this.multiline, icon: PluginIcon = this.icon): Input = Input(title, value, actionId, secret, multiline, icon, symbol, trailingAction)
    }
    @Serializable
    data class Chat(val messages: List<ChatMessage>, val sendAction: String, val stopAction: String,
        val running: Boolean = false, val placeholder: String = "发送消息",
        val conversationId: String = "", val controls: List<ChatControl> = emptyList(), val approval: ChatApproval? = null,
        val backgroundRunning: Boolean = false, val imageAction: String? = null,
        val draftImages: List<ChatImage> = emptyList(), val removeImageAction: String? = null,
        val queueAction: String? = null, val steerAction: String? = null, val queued: List<ChatQueuedMessage> = emptyList(),
        val historyAction: String? = null, val historyTotal: Int = 0,
        val fileAction: String? = null, val draftFiles: List<ChatFile> = emptyList(), val removeFileAction: String? = null) : PageBlock {
        constructor(messages: List<ChatMessage>, sendAction: String, stopAction: String,
        running: Boolean = false, placeholder: String = "发送消息",
        conversationId: String = "", controls: List<ChatControl> = emptyList(), approval: ChatApproval? = null,
        backgroundRunning: Boolean = false, imageAction: String? = null,
        draftImages: List<ChatImage> = emptyList(), removeImageAction: String? = null,
        queueAction: String? = null, steerAction: String? = null, queued: List<ChatQueuedMessage> = emptyList(),
        historyAction: String? = null, historyTotal: Int = 0)
            : this(messages, sendAction, stopAction, running, placeholder, conversationId, controls, approval, backgroundRunning, imageAction, draftImages, removeImageAction, queueAction, steerAction, queued, historyAction, historyTotal, null, emptyList(), null)
        fun copy(messages: List<ChatMessage> = this.messages, sendAction: String = this.sendAction, stopAction: String = this.stopAction,
        running: Boolean = this.running, placeholder: String = this.placeholder,
        conversationId: String = this.conversationId, controls: List<ChatControl> = this.controls, approval: ChatApproval? = this.approval,
        backgroundRunning: Boolean = this.backgroundRunning, imageAction: String? = this.imageAction,
        draftImages: List<ChatImage> = this.draftImages, removeImageAction: String? = this.removeImageAction,
        queueAction: String? = this.queueAction, steerAction: String? = this.steerAction, queued: List<ChatQueuedMessage> = this.queued,
        historyAction: String? = this.historyAction, historyTotal: Int = this.historyTotal): Chat =
            Chat(messages, sendAction, stopAction, running, placeholder, conversationId, controls, approval, backgroundRunning, imageAction, draftImages, removeImageAction, queueAction, steerAction, queued, historyAction, historyTotal, fileAction, draftFiles, removeFileAction)
        constructor(messages: List<ChatMessage>, sendAction: String, stopAction: String, running: Boolean = false, placeholder: String = "发送消息",
            conversationId: String = "", controls: List<ChatControl> = emptyList(), approval: ChatApproval? = null, backgroundRunning: Boolean = false)
            : this(messages, sendAction, stopAction, running, placeholder, conversationId, controls, approval, backgroundRunning, null, emptyList(), null, null, null, emptyList())
        fun copy(messages: List<ChatMessage> = this.messages, sendAction: String = this.sendAction, stopAction: String = this.stopAction,
            running: Boolean = this.running, placeholder: String = this.placeholder, conversationId: String = this.conversationId,
            controls: List<ChatControl> = this.controls, approval: ChatApproval? = this.approval, backgroundRunning: Boolean = this.backgroundRunning): Chat =
            Chat(messages, sendAction, stopAction, running, placeholder, conversationId, controls, approval, backgroundRunning, imageAction, draftImages, removeImageAction, queueAction, steerAction, queued, historyAction, historyTotal, fileAction, draftFiles, removeFileAction)
        constructor(messages: List<ChatMessage>, sendAction: String, stopAction: String, running: Boolean = false, placeholder: String = "发送消息")
            : this(messages, sendAction, stopAction, running, placeholder, "", emptyList(), null, false)
        fun copy(messages: List<ChatMessage> = this.messages, sendAction: String = this.sendAction, stopAction: String = this.stopAction,
            running: Boolean = this.running, placeholder: String = this.placeholder): Chat =
            Chat(messages, sendAction, stopAction, running, placeholder, conversationId, controls, approval, backgroundRunning, imageAction, draftImages, removeImageAction, queueAction, steerAction, queued, historyAction, historyTotal, fileAction, draftFiles, removeFileAction)
    }
    @Serializable
    data class Text(val text: String, val heading: Boolean = false) : PageBlock
    /** Theme-aware native GFM. Images are explicit scoped resources keyed by their Markdown URL. */
    @Serializable
    data class Markdown(val text: String, val images: Map<String, PluginResource> = emptyMap()) : PageBlock
    @Serializable
    data class Gallery(val images: List<PageImage>) : PageBlock
    @Serializable
    data class Notice(val text: String) : PageBlock
    @Serializable
    data class Actions(val actions: List<PageAction>) : PageBlock
    @Serializable
    data class Group(val title: String, val children: List<PageBlock>, val collapsible: Boolean = false,
        val initiallyExpanded: Boolean = false) : PageBlock {
        constructor(title: String, children: List<PageBlock>) : this(title, children, false, false)
        fun copy(title: String = this.title, children: List<PageBlock> = this.children): Group =
            Group(title, children, collapsible, initiallyExpanded)
    }
}

@Serializable
data class PageImage(val resource: PluginResource, val description: String = "", val width: Int? = null, val height: Int? = null) {
    constructor(resource: PluginResource, description: String = "") : this(resource, description, null, null)
    fun copy(resource: PluginResource = this.resource, description: String = this.description): PageImage = PageImage(resource, description, width, height)
    init { require((width == null && height == null) || (width != null && height != null && width > 0 && height > 0)) }
}
