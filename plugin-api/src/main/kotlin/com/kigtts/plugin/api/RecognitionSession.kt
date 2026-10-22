package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable enum class RecognitionPurpose { CAPTIONS, VOICE_INPUT }
@Serializable enum class CaptionOutput { PANEL, FLOATING, BOTH }

/** Service chooses policy. The host only captures audio and renders the selected output. */
@Serializable data class RecognitionSession(val serviceId: String, val purpose: RecognitionPurpose,
    val config: RecognitionConfig = RecognitionConfig(), val continuous: Boolean = false,
    val output: CaptionOutput = CaptionOutput.PANEL, val disableAutomaticRead: Boolean = false,
    val readFinalized: Boolean = false)
