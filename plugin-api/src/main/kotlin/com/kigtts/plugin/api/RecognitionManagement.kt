package com.kigtts.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class AsrSettingsPage(val id: String, val title: String, val icon: PluginIcon = PluginIcon.MIC)
@Serializable
data class AsrProvider(val pluginId: String, val name: String, val info: AsrInfo,
    val enabled: Boolean = true, val settingsPages: List<AsrSettingsPage> = emptyList())

/** Only the recognition service receives this mediated protocol. Implementations remain in provider processes. */
interface RecognitionBroker {
    fun providers(): List<AsrProvider>
    suspend fun control(pluginId: String, command: AsrControl): Boolean = false
    suspend fun recognize(pluginId: String, audio: PcmAudio, config: RecognitionConfig): RecognitionResult
    fun recognizeStream(pluginId: String, audio: Flow<PcmAudio>, config: RecognitionConfig): Flow<RecognitionEvent>
}
