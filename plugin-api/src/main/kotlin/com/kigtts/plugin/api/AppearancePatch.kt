package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Unspecified values inherit the host appearance. Background resources must belong to the caller. */
@Serializable
data class AppearancePatch(
    val themeColor: Long? = null,
    val mode: AppearanceMode? = null,
    val background: AppearanceBackground? = null,
    val backgroundDim: Float? = null,
    val pureBlackDark: Boolean? = null,
    val contrast: AppearanceContrast? = null,
) {
    init {
        require(themeColor == null || themeColor in 0..0xffffffffL)
        require(backgroundDim == null || backgroundDim in 0f..1f)
    }
}

@Serializable enum class AppearanceMode { SYSTEM, LIGHT, DARK }
@Serializable enum class AppearanceContrast { DEFAULT, MEDIUM, HIGH }

@Serializable
sealed interface AppearanceBackground {
    @Serializable data object Clear : AppearanceBackground
    @Serializable data class Image(val source: PluginResource) : AppearanceBackground
}
