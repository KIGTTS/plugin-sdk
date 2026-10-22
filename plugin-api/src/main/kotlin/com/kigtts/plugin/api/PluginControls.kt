package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable
data class PluginControlOption(val id: String, val label: String)

@Serializable
enum class PluginChoiceStyle { SEGMENTED, SELECT }

/** UI-free native settings contract. The host serializes slider values as decimal floats,
 * toggles as true/false, choices as option IDs and colors as unsigned ARGB decimal longs.
 */
@Serializable
sealed interface PluginControl {
    val title: String
    val actionId: String

    @Serializable
    data class Slider(override val title: String, override val actionId: String, val value: Float,
        val min: Float = 0f, val max: Float = 1f, val steps: Int = 0, val suffix: String = "") : PluginControl

    @Serializable
    data class Toggle(override val title: String, override val actionId: String, val checked: Boolean) : PluginControl

    @Serializable
    data class Choice(override val title: String, override val actionId: String,
        val options: List<PluginControlOption>, val selected: String,
        val style: PluginChoiceStyle = PluginChoiceStyle.SEGMENTED) : PluginControl

    /** Opaque native HSV/HEX picker; the host supplies a color swatch and opens its picker. */
    @Serializable
    data class Color(override val title: String, override val actionId: String, val value: Long) : PluginControl
}
