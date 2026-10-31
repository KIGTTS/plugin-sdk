package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Reversible photo treatment. A null style on a card preserves legacy baked photographs. */
@Serializable data class PolaroidPhotoStyle(
    val filter: String = "ORIGINAL", val strength: Float = 1f,
    val vignette: Float = 0f, val grain: Float = 0f, val aberration: Float = 0f, val bloom: Float = 0f,
    val lutImageId: String? = null, val lutSize: Int = 0,
    val lutMin: List<Float> = listOf(0f, 0f, 0f), val lutMax: List<Float> = listOf(1f, 1f, 1f)
)
