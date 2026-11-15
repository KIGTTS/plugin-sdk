package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

import kotlinx.coroutines.flow.Flow

/** Signed 16-bit little-endian PCM. Chunk boundaries must align with a full sample frame. */
@Serializable
data class PcmAudio(val bytes: ByteArray, val sampleRate: Int = 16_000, val channels: Int = 1) {
    init {
        require(sampleRate in 8_000..192_000 && channels in 1..2)
        require(bytes.size % (2 * channels) == 0) { "PCM must contain complete sample frames" }
    }
}

@Serializable
data class AudioFormat(val sampleRate: Int = 16_000, val channels: Int = 1) {
    init { require(sampleRate in 8_000..192_000 && channels in 1..2) }
}
@Serializable
enum class EngineAvailability { READY, NEEDS_RESOURCES }
@Serializable
enum class StreamingKind { NONE, SEGMENTED, INCREMENTAL }
@Serializable
enum class AsrRole { PROVIDER, SERVICE }
@Serializable
data class VoiceInfo(val id: String, val name: String, val language: String,
    val description: String = "", val deletable: Boolean = false, val packName: String? = null, val packId: String? = null)
@Serializable
enum class TtsRole { PROVIDER, SERVICE }
@Serializable
enum class TtsProfile { REALTIME, PREBAKE }
/** Stable, non-secret fingerprints of the provider's synthesis settings for each profile.
 * Keep shared voice parameters in VoicePackInfo; a change here invalidates only the matching profile.
 */
@Serializable
data class TtsProfileVersions(val realtime: String, val prebake: String) {
    init { require(realtime.length in 1..128 && prebake.length in 1..128) }
}
@Serializable
data class TtsInfo(val availability: EngineAvailability = EngineAvailability.READY,
    val voices: List<VoiceInfo> = emptyList(), val maxTextLength: Int = 4_000,
    val role: TtsRole = TtsRole.PROVIDER, val selectedVoiceId: String? = null,
    val importsVoicePacks: Boolean = false, val managesVoices: Boolean = false,
    val packs: List<VoicePackInfo> = emptyList(), val supportsControl: Boolean = false,
    val profiles: TtsProfileVersions? = null) {
    init { require(maxTextLength in 2..100_000) }
    // Keep the JVM signatures used by API 9–34 packages, including default-argument masks.
    constructor(availability: EngineAvailability = EngineAvailability.READY, voices: List<VoiceInfo> = emptyList(),
        maxTextLength: Int = 4_000, role: TtsRole = TtsRole.PROVIDER, selectedVoiceId: String? = null,
        importsVoicePacks: Boolean = false, managesVoices: Boolean = false,
        packs: List<VoicePackInfo> = emptyList(), supportsControl: Boolean = false) :
        this(availability, voices, maxTextLength, role, selectedVoiceId, importsVoicePacks, managesVoices, packs, supportsControl, null)
    fun copy(availability: EngineAvailability = this.availability, voices: List<VoiceInfo> = this.voices,
        maxTextLength: Int = this.maxTextLength, role: TtsRole = this.role, selectedVoiceId: String? = this.selectedVoiceId,
        importsVoicePacks: Boolean = this.importsVoicePacks, managesVoices: Boolean = this.managesVoices,
        packs: List<VoicePackInfo> = this.packs, supportsControl: Boolean = this.supportsControl) =
        TtsInfo(availability, voices, maxTextLength, role, selectedVoiceId, importsVoicePacks, managesVoices, packs, supportsControl, profiles)
}
@Serializable
data class AsrInfo(val inputFormat: AudioFormat = AudioFormat(), val batch: Boolean = true,
    val streaming: StreamingKind = StreamingKind.INCREMENTAL,
    val availability: EngineAvailability = EngineAvailability.READY, val maxBatchSeconds: Int = 30,
    val role: AsrRole = AsrRole.PROVIDER) {
    init { require(maxBatchSeconds in 1..120 && (batch || streaming != StreamingKind.NONE)) }
}
@Serializable
data class TtsRequest(val text: String, val language: String = "zh-CN", val rate: Float = 1f,
    val voiceId: String? = null, val profile: TtsProfile = TtsProfile.REALTIME) {
    init { require(rate.isFinite() && rate in 0.25f..4f) }
    constructor(text: String, language: String = "zh-CN", rate: Float = 1f, voiceId: String? = null) :
        this(text, language, rate, voiceId, TtsProfile.REALTIME)
    fun copy(text: String = this.text, language: String = this.language, rate: Float = this.rate, voiceId: String? = this.voiceId) =
        TtsRequest(text, language, rate, voiceId, profile)
}
@Serializable
enum class EngineError { NOT_CONFIGURED, UNSUPPORTED_FORMAT, INITIALIZATION, INFERENCE, TIMEOUT, UNAVAILABLE }
class EngineException(val code: EngineError, message: String, cause: Throwable? = null) : Exception(message, cause)
@Serializable
sealed interface SynthesisEvent {
    @Serializable
    data object Started : SynthesisEvent
    @Serializable
    data class Audio(val audio: PcmAudio) : SynthesisEvent
    @Serializable
    data class Finished(val notice: String? = null) : SynthesisEvent
}

interface TtsEngine {
    val info: TtsInfo get() = TtsInfo()
    /** Cancellation releases request state; reusable models may remain until idle or RELEASE. */
    fun synthesize(request: TtsRequest): Flow<SynthesisEvent>
    suspend fun control(command: TtsControl) {}
    suspend fun close() {}
}

@Serializable enum class TtsControlOperation { PREPARE, RELEASE, SET_RESIDENT, ACTIVITY }
@Serializable data class TtsControl(val operation: TtsControlOperation, val voiceId: String? = null,
    val resident: Boolean = false, val active: Boolean = false)

@Serializable
data class RecognitionConfig(val language: String = "zh-CN", val maxDisplayCharacters: Int = 12_000) {
    init { require(maxDisplayCharacters in 256..32_000) }
}
@Serializable
data class RecognitionResult(val text: String, val placeholder: Boolean = false)
@Serializable
sealed interface RecognitionEvent {
    @Serializable
    data object Started : RecognitionEvent
    @Serializable
    data class Partial(val text: String) : RecognitionEvent
    @Serializable
    data class Final(val result: RecognitionResult) : RecognitionEvent
    /** Service-owned immutable prefix + replaceable hypothesis, bounded for UI/IPC. Finals have monotonic sequence numbers. */
    @Serializable
    data class Transcript(val committed: String, val partial: String, val sequence: Long,
        val finalized: String? = null) : RecognitionEvent
}

interface AsrEngine {
    val info: AsrInfo get() = AsrInfo()
    /** One complete recording, one result. Cancellation propagates to the engine. */
    suspend fun recognize(audio: PcmAudio, config: RecognitionConfig = RecognitionConfig()): RecognitionResult
    /** Cold flow with backpressure. Final may end each utterance; EOF flushes the last utterance.
     * SEGMENTED means offline inference with endpointing, not an incremental neural decoder. */
    fun recognizeStream(audio: Flow<PcmAudio>, config: RecognitionConfig = RecognitionConfig()): Flow<RecognitionEvent>
    suspend fun close() {}
}
