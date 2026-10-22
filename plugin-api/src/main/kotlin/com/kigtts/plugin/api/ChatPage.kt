package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable enum class ChatRole { USER, ASSISTANT, TOOL }
@Serializable enum class ChatMessageState { COMPLETE, STREAMING, FAILED, CANCELLED }
@Serializable data class ChatImage(val source: PluginResource, val description: String = "图片")
@Serializable data class ChatQueuedMessage(val id: String, val text: String, val removeAction: String, val frontAction: String,
    val steerAction: String? = null, val reorderAction: String? = null) {
    constructor(id: String, text: String, removeAction: String, frontAction: String) : this(id, text, removeAction, frontAction, null, null)
    fun copy(id: String = this.id, text: String = this.text, removeAction: String = this.removeAction, frontAction: String = this.frontAction) =
        ChatQueuedMessage(id, text, removeAction, frontAction, steerAction, reorderAction)
}
@Serializable data class ChatMessage(val id: String, val role: ChatRole, val text: String,
    val reasoning: String = "", val state: ChatMessageState = ChatMessageState.COMPLETE,
    val summary: String = "", val toolName: String = "", val detail: String = "", val icon: PluginIcon = PluginIcon.INFO,
    val images: List<ChatImage> = emptyList(), val thinking: Boolean = false, val position: Int = -1,
    val hasReasoning: Boolean = false) {
    // Keep the first API11 constructor/copy and their Kotlin default bridges for installed 0.9.1 packages.
    constructor(id: String, role: ChatRole, text: String, reasoning: String = "", state: ChatMessageState = ChatMessageState.COMPLETE,
        summary: String = "", toolName: String = "", detail: String = "", icon: PluginIcon = PluginIcon.INFO,
        images: List<ChatImage> = emptyList(), thinking: Boolean = false, position: Int = -1)
        : this(id, role, text, reasoning, state, summary, toolName, detail, icon, images, thinking, position, reasoning.isNotBlank())
    fun copy(id: String = this.id, role: ChatRole = this.role, text: String = this.text, reasoning: String = this.reasoning,
        state: ChatMessageState = this.state, summary: String = this.summary, toolName: String = this.toolName,
        detail: String = this.detail, icon: PluginIcon = this.icon, images: List<ChatImage> = this.images,
        thinking: Boolean = this.thinking, position: Int = this.position): ChatMessage =
        ChatMessage(id, role, text, reasoning, state, summary, toolName, detail, icon, images, thinking, position, hasReasoning)
    constructor(id: String, role: ChatRole, text: String, reasoning: String = "", state: ChatMessageState = ChatMessageState.COMPLETE,
        summary: String = "", toolName: String = "", detail: String = "", icon: PluginIcon = PluginIcon.INFO)
        : this(id, role, text, reasoning, state, summary, toolName, detail, icon, emptyList(), false)
    fun copy(id: String = this.id, role: ChatRole = this.role, text: String = this.text, reasoning: String = this.reasoning,
        state: ChatMessageState = this.state, summary: String = this.summary, toolName: String = this.toolName,
        detail: String = this.detail, icon: PluginIcon = this.icon): ChatMessage =
        ChatMessage(id, role, text, reasoning, state, summary, toolName, detail, icon, images, thinking, position, hasReasoning)
    constructor(id: String, role: ChatRole, text: String, reasoning: String = "", state: ChatMessageState = ChatMessageState.COMPLETE)
        : this(id, role, text, reasoning, state, "", "", "", PluginIcon.INFO)
    fun copy(id: String = this.id, role: ChatRole = this.role, text: String = this.text,
        reasoning: String = this.reasoning, state: ChatMessageState = this.state): ChatMessage =
        ChatMessage(id, role, text, reasoning, state, summary, toolName, detail, icon, images, thinking, position, hasReasoning)
}
@Serializable enum class ChatControlKind { MODEL, ACCESS, PROVIDER, REASONING }
@Serializable data class ChatOption(val action: String, val label: String, val value: String = "", val section: String = "") {
    constructor(action: String, label: String, value: String = "") : this(action, label, value, "")
    fun copy(action: String = this.action, label: String = this.label, value: String = this.value) = ChatOption(action, label, value, section)
}
@Serializable data class ChatControl(val title: String, val options: List<ChatOption>, val selected: String,
    val kind: ChatControlKind = ChatControlKind.MODEL)
@Serializable data class ChatApproval(val title: String, val approveAction: String, val rejectAction: String,
    val detail: String = "", val reviewAction: String? = null)
