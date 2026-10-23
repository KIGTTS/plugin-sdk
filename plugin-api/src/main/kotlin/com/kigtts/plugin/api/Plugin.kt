package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

const val PLUGIN_API_VERSION = 26
const val MIN_PLUGIN_API_VERSION = 9

@Serializable
enum class PluginRequirement { RECOGNITION_SERVICE }

@Serializable
data class PluginDescriptor(
    val id: String,
    val name: String,
    val description: String,
    val version: String = "0.1.0",
    val apiVersion: Int = PLUGIN_API_VERSION,
    val placeholder: Boolean = false,
    val license: String = "MIT",
    val requires: List<PluginRequirement> = emptyList(),
)

/** UI-free contract: contributions are rendered by the host's Material 3 theme. */
interface KigPlugin {
    val localization: PluginLocalization get() = PluginLocalization()
    /** Local UI revisions; workers relay these without reloading implementations or replaying page entry. */
    val uiChanges: kotlinx.coroutines.flow.Flow<Long> get() = kotlinx.coroutines.flow.emptyFlow()
    val descriptor: PluginDescriptor
    val contributions: PluginContributions get() = PluginContributions()
    fun contributions(context: PluginContext): PluginContributions = contributions
    val tts: TtsEngine? get() = null
    val asr: AsrEngine? get() = null
    val voiceManager: VoiceManager? get() = null
    suspend fun execute(actionId: String, context: PluginContext): PluginOutcome
    fun page(pageId: String, context: PluginContext): PluginPage? = null
    /** Called once before any other operation. All mutable files belong to this environment. */
    suspend fun attach(environment: PluginEnvironment) {}
    /** Host cancels and joins active sessions before closing/unloading a plugin. */
    suspend fun close() { tts?.close(); asr?.close() }
}

@Serializable
data class PluginContext(val editorText: String, val transcript: String, val listening: Boolean = false,
    val listeningPanelVisible: Boolean = listening, val inputs: Map<String, String> = emptyMap(),
    val contentKind: String = "TEXT", val contentId: String? = null,
    val automaticRead: Boolean = false, val ttsPluginId: String? = null, val speechActive: Boolean = false,
    val voiceInputActive: Boolean = false, val continuousRecognition: Boolean = false,
    val recognitionServiceId: String? = null, val locale: String = "zh-CN")

@Serializable
enum class HostCommand { TOGGLE_LISTENING, CLOSE_LISTENING_PANEL, USE_STREAMING, USE_BATCH, VOICE_INPUT, TOGGLE_SUBTITLE_FULLSCREEN,
    /** Show the secondary panel without starting the microphone or changing recognition state. */
    OPEN_LISTENING_PANEL }

@Serializable
sealed interface PluginOutcome {
    /** Recognized foreground gesture or explicit user action selects an existing quick text by ID.
     * The host re-resolves the entry and uses its normal send path (including media and reading).
     * Requires the plugin's quick-text-read grant; never caches a copy of a deleted entry.
     */
    @Serializable
    data class SendQuickText(val entryId: String) : PluginOutcome
    /** A sampled gesture did not match; no navigation, message or UI invalidation. */
    @Serializable
    data object NoAction : PluginOutcome
    /** Inspect a downloaded package and ask the user to trust its publisher in native host UI. */
    @Serializable
    data class InstallPackage(val resource: PluginResource, val allowDowngrade: Boolean = false,
        val dependencies: List<PluginResource> = emptyList()) : PluginOutcome
    @Serializable
    data class StartRecognition(val session: RecognitionSession) : PluginOutcome
    @Serializable
    data class EndRecognition(val submit: Boolean = false) : PluginOutcome
    @Serializable
    data class PresentContent(val content: SubtitleContent) : PluginOutcome
    /** Session-only local renderer for TEXT subtitles; null disables this plugin's renderer.
     * Receives window.KigttsSubtitle.update({text, placeholder, fontSizePercent}) without reloading.
     * Does not replace the editable document or present a media quick text. No network access.
     */
    @Serializable
    data class SetSubtitleRenderer(val root: PluginResource? = null, val mainPage: String = "index.html",
        val presentation: PanelPresentation = PanelPresentation()) : PluginOutcome {
        constructor(root: PluginResource? = null, mainPage: String = "index.html") : this(root, mainPage, PanelPresentation())
        fun copy(root: PluginResource? = this.root, mainPage: String = this.mainPage): SetSubtitleRenderer = SetSubtitleRenderer(root, mainPage, presentation)
    }
    /** Session-only secondary panel owned by this plugin. Null restores this owner's native panel.
     * pageId resolves through page(); Browser and native blocks share normal permissions/actions.
     * replaceContent=false overlays the original captions. replaceControls filters bottom slots
     * to this owner and hides native font controls. The host keeps its resize/dismiss affordance.
     */
    @Serializable
    data class SetListeningPanel(val pageId: String? = null, val replaceContent: Boolean = true,
        val replaceControls: Boolean = true, val fadeControlsWhenIdle: Boolean = false) : PluginOutcome {
        constructor(pageId: String? = null, replaceContent: Boolean = true, replaceControls: Boolean = true)
            : this(pageId, replaceContent, replaceControls, false)
        fun copy(pageId: String? = this.pageId, replaceContent: Boolean = this.replaceContent, replaceControls: Boolean = this.replaceControls): SetListeningPanel =
            SetListeningPanel(pageId, replaceContent, replaceControls, fadeControlsWhenIdle)
    }
    @Serializable
    data class SelectTtsService(val pluginId: String) : PluginOutcome
    @Serializable
    data class SelectRecognitionService(val pluginId: String) : PluginOutcome
    @Serializable
    data class OpenPluginPage(val pluginId: String, val pageId: String) : PluginOutcome
    @Serializable
    data class SetPluginEnabled(val pluginId: String, val enabled: Boolean) : PluginOutcome
    @Serializable
    data object RecognitionRoutingChanged : PluginOutcome
    @Serializable
    data object RefreshUi : PluginOutcome
    @Serializable
    data class HostAction(val command: HostCommand) : PluginOutcome
    @Serializable
    data class Message(val text: String) : PluginOutcome
    @Serializable
    data class Dialog(val title: String, val text: String) : PluginOutcome
    @Serializable
    data class Menu(val title: String, val items: List<PageAction>) : PluginOutcome
    /** Native, internally scrolling settings. Changes call actionId with inputs["value"].
     * Returning NoAction keeps the panel open; returning Controls replaces its values.
     */
    @Serializable
    data class Controls(val title: String, val controls: List<PluginControl>, val resetActionId: String? = null) : PluginOutcome
    /** A real native confirmation, with actions in the dialog footer rather than a menu body. */
    @Serializable
    data class Confirm(val title: String, val text: String, val action: PageAction, val cancelLabel: String = "取消") : PluginOutcome
    /** User-facing camera capture; the original image is copied into this plugin's directory. */
    @Serializable
    data class CapturePhoto(val title: String, val directory: String, val actionId: String) : PluginOutcome
    @Serializable
    data class OpenPage(val pageId: String) : PluginOutcome
    @Serializable
    data class ReplaceEditorText(val text: String) : PluginOutcome
    /** Host chooses and copies a document; callback receives inputs["resourcePath"]. */
    @Serializable
    data class ImportResource(val title: String, val directory: String, val actionId: String,
        val zip: Boolean = false, val mimeTypes: List<String> = listOf("*/*"), val multiple: Boolean = false) : PluginOutcome
    /** Host renders a native input dialog; callback receives inputs["value"]. */
    @Serializable
    data class TextInput(val title: String, val actionId: String, val initialValue: String = "", val multiline: Boolean = false,
        val secret: Boolean = false) : PluginOutcome
    @Serializable
    data class ExportResource(val title: String, val resource: PluginResource, val fileName: String,
        val mimeType: String = "application/octet-stream", val temporary: Boolean = false) : PluginOutcome
}
