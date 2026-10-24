package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Optional resource control. Legacy recognition engines remain usable without this interface. */
interface ControlledAsrEngine : AsrEngine {
    suspend fun control(command: AsrControl): Boolean
}
@Serializable
enum class AsrControlOp { PREPARE, RELEASE, SET_RESIDENT }
@Serializable
data class AsrControl(val op: AsrControlOp, val resident: Boolean? = null)
