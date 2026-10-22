package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject

/** A connection-bound capability. JSON cannot impersonate another plugin or choose its storage owner. */
interface AppBroker { suspend fun request(command: AppCommand): AppReply }

@Serializable sealed interface AppCommand {
    /** Caller-owned notification lifecycle. Posting requests declared notifications + Android grants. */
    @Serializable @SerialName("notifications") data class Notifications(val operation: String = "status",
        val notification: PluginNotification? = null, val id: String? = null) : AppCommand
    /** Native symbol catalog and caller-scoped SVG rendering / durable icon imports. */
    @Serializable @SerialName("ui-assets") data class UiAssets(val operation: String, val source: PluginResource? = null,
        val destination: String? = null, val query: String = "", val offset: Int = 0, val size: Int = 256) : AppCommand
    /** Requests the caller's own declared capability; only source runtimes may name an owned project. */
    @Serializable @SerialName("permission") data class Permission(val permission: String, val projectId: String? = null) : AppCommand
    /** Trusted source runtime forwards a sandbox project's operation with its separate permission identity. */
    @Serializable @SerialName("managed-call") data class ManagedCall(val projectId: String, val channel: String, val command: JsonObject) : AppCommand
    /** Render a published page (or the caller's own plugin browser page) to a bounded image in caller storage. */
    @Serializable @SerialName("browser-capture") data class BrowserCapture(val destination: String,
        val mediaId: String? = null, val pageId: String? = null, val width: Int = 960, val height: Int = 1280) : AppCommand
    /** A separate, caller-owned WebView session. Native device/action bridges are unavailable here. */
    @Serializable @SerialName("browser-session") data class BrowserSession(val operation: String, val sessionId: String? = null,
        val mediaId: String? = null, val pageId: String? = null, val width: Int = 960, val height: Int = 1280,
        val selector: String? = null, val text: String = "", val x: Float? = null, val y: Float? = null,
        val deltaY: Float = 0f, val waitMs: Int = 800, val timeoutMs: Int = 15000, val destination: String? = null) : AppCommand
    @Serializable @SerialName("describe") data object Describe : AppCommand
    @Serializable @SerialName("state") data object State : AppCommand
    @Serializable @SerialName("navigate") data class Navigate(val target: String) : AppCommand
    @Serializable @SerialName("text") data class SetText(val text: String) : AppCommand
    @Serializable @SerialName("automatic-read") data class SetAutomaticRead(val enabled: Boolean) : AppCommand
    @Serializable @SerialName("speak") data object Speak : AppCommand
    @Serializable @SerialName("stop-speech") data object StopSpeech : AppCommand
    @Serializable @SerialName("setting") data class Setting(val key: String, val value: String) : AppCommand
    @Serializable @SerialName("plugin-action") data class PluginAction(val pluginId: String, val actionId: String,
        val inputs: Map<String, String> = emptyMap()) : AppCommand
    @Serializable @SerialName("plugin-call") data class PluginCall(val pluginId: String, val actionId: String,
        val inputs: Map<String, String> = emptyMap()) : AppCommand
    /** Copy a scoped draft into the recipient's inbox. The recipient still owns review and installation. */
    @Serializable @SerialName("deliver-resource") data class DeliverResource(val pluginId: String,
        val source: PluginResource, val actionId: String) : AppCommand
}
@Serializable sealed interface AppReply {
    @Serializable @SerialName("data") data class Data(val value: JsonObject) : AppReply
    @Serializable @SerialName("failure") data class Failure(val code: String, val message: String) : AppReply
}
