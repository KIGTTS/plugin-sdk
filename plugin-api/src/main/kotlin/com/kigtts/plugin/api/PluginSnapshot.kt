package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PluginSnapshot(val descriptor: PluginDescriptor, val contributions: PluginContributions,
    val pages: List<PluginPage>, val tts: TtsInfo? = null, val asr: AsrInfo? = null,
    val localization: PluginLocalization = PluginLocalization()) {
    init { validateSnapshot(this) }
}

/** Shared wire types; implementations/classes never cross the process boundary. */
val PluginJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
