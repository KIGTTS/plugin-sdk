package com.kigtts.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class TtsProvider(val pluginId: String, val name: String, val info: TtsInfo, val enabled: Boolean = true,
    val generation: String = "")

@Serializable
enum class VoiceOperation { IMPORT, SELECT, DELETE, RENAME, REFRESH, UPDATE_METADATA, SET_AVATAR, EXPORT, REORDER }
@Serializable
data class VoiceCommand(val operation: VoiceOperation, val voiceId: String? = null,
    val resourcePath: String? = null, val value: String? = null, val packId: String? = null,
    val metadata: VoicePackMetadata? = null, val order: List<String> = emptyList())

@Serializable
data class VoicePackMetadata(val name: String, val remark: String = "", val pinned: Boolean = false, val order: Long = 0)
@Serializable
data class VoicePackInfo(val id: String, val metadata: VoicePackMetadata, val voiceIds: List<String>,
    val avatarPath: String? = null, val editable: Boolean = false, val exportable: Boolean = false, val deletable: Boolean = false)

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
