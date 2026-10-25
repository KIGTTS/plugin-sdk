package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** card-frames.json, only for CARD_FRAME resources. Artwork may be a local SVG or bitmap. */
@Serializable data class PersonalCardFramePackage(val schemaVersion: Int = 1, val items: List<PersonalCardFrameResource>)
@Serializable data class PersonalCardFrameResource(val id: String, val name: String, val artwork: String)
