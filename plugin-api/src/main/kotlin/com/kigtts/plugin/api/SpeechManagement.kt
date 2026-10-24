package com.kigtts.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class TtsProvider(val pluginId: String, val name: String, val info: TtsInfo, val enabled: Boolean = true,
    val generation: String = "", val settingsPages: List<AsrSettingsPage> = emptyList()) {
    // Keep constructors and copy masks used by installed synthesis services.
    constructor(pluginId: String, name: String, info: TtsInfo, enabled: Boolean = true, generation: String = "")
        : this(pluginId, name, info, enabled, generation, emptyList())
    fun copy(pluginId: String = this.pluginId, name: String = this.name, info: TtsInfo = this.info,
        enabled: Boolean = this.enabled, generation: String = this.generation): TtsProvider =
        TtsProvider(pluginId, name, info, enabled, generation, settingsPages)
}

@Serializable
enum class VoiceOperation { IMPORT, SELECT, DELETE, RENAME, REFRESH, UPDATE_METADATA, SET_AVATAR, EXPORT, REORDER,
    /** packId identifies the pack; value is a JSON object of parameter IDs to numeric values. */
    UPDATE_PARAMETERS }
@Serializable
data class VoiceCommand(val operation: VoiceOperation, val voiceId: String? = null,
    val resourcePath: String? = null, val value: String? = null, val packId: String? = null,
    val metadata: VoicePackMetadata? = null, val order: List<String> = emptyList())

@Serializable
data class VoicePackMetadata(val name: String, val remark: String = "", val pinned: Boolean = false, val order: Long = 0)
@Serializable
data class VoicePackInfo(val id: String, val metadata: VoicePackMetadata, val voiceIds: List<String>,
    val avatarPath: String? = null, val editable: Boolean = false, val exportable: Boolean = false, val deletable: Boolean = false,
    val parameters: List<VoiceParameter> = emptyList(), val providerLabel: String = "") {
    init { require(providerLabel.length <= 32); require(parameters.size <= 32 && parameters.map { it.id }.distinct().size == parameters.size) }
    constructor(id: String, metadata: VoicePackMetadata, voiceIds: List<String>, avatarPath: String? = null,
        editable: Boolean = false, exportable: Boolean = false, deletable: Boolean = false,
        parameters: List<VoiceParameter> = emptyList())
        : this(id, metadata, voiceIds, avatarPath, editable, exportable, deletable, parameters, "")
    fun copy(id: String = this.id, metadata: VoicePackMetadata = this.metadata, voiceIds: List<String> = this.voiceIds,
        avatarPath: String? = this.avatarPath, editable: Boolean = this.editable, exportable: Boolean = this.exportable,
        deletable: Boolean = this.deletable, parameters: List<VoiceParameter> = this.parameters): VoicePackInfo =
        VoicePackInfo(id, metadata, voiceIds, avatarPath, editable, exportable, deletable, parameters, providerLabel)
    // Preserve installed API9-20 constructors/default masks and copy methods.
    constructor(id: String, metadata: VoicePackMetadata, voiceIds: List<String>, avatarPath: String? = null,
        editable: Boolean = false, exportable: Boolean = false, deletable: Boolean = false)
        : this(id, metadata, voiceIds, avatarPath, editable, exportable, deletable, emptyList())
    fun copy(id: String = this.id, metadata: VoicePackMetadata = this.metadata, voiceIds: List<String> = this.voiceIds,
        avatarPath: String? = this.avatarPath, editable: Boolean = this.editable, exportable: Boolean = this.exportable,
        deletable: Boolean = this.deletable): VoicePackInfo =
        VoicePackInfo(id, metadata, voiceIds, avatarPath, editable, exportable, deletable, parameters, providerLabel)
}

/** Provider-owned settings. Values are scoped to one pack and exclude the service's global rate. */
@Serializable
data class VoiceParameter(val id: String, val title: String, val value: Float,
    val min: Float, val max: Float, val defaultValue: Float, val steps: Int = 0, val suffix: String = "") {
    init {
        require(id.matches(Regex("[a-zA-Z][a-zA-Z0-9_.-]{0,47}")) && title.length in 1..80)
        require(min.isFinite() && max.isFinite() && min < max && value.isFinite() && value in min..max)
        require(defaultValue.isFinite() && defaultValue in min..max && steps in 0..1000)
    }
}

/** Each provider parses, validates and stores its own packs. Host never knows a voice file format. */
interface VoiceManager {
    suspend fun command(command: VoiceCommand): PluginOutcome
}

/** Granted only to a synthesis service. Calls are mediated by the host; no implementation is shared. */
interface SpeechBroker {
    fun providers(): List<TtsProvider>
    suspend fun command(pluginId: String, command: VoiceCommand): PluginOutcome
    fun synthesize(pluginId: String, request: TtsRequest): Flow<SynthesisEvent>
    suspend fun control(pluginId: String, command: TtsControl) {}
}
