package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable data class PluginFileItem(val path: String, val name: String, val directory: Boolean, val bytes: Long = 0)
