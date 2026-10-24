package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Normalized coordinates shared by a stroke icon and its drawing guide. */
@Serializable
data class StrokePoint(val x: Float, val y: Float)
