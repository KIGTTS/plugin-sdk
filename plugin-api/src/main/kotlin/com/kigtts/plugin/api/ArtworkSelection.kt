package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** API30: explicit user choice; no automatic access to the rest of any library. */
@Serializable enum class ArtworkLibrary { POLAROIDS, PERSONAL_CARDS, SKETCHES }
@Serializable data class ArtworkSelectionRequest(val library: ArtworkLibrary, val title: String = "选择作品",
    val multiple: Boolean = false, val selectedIds: List<String> = emptyList(), val actionId: String = "artwork-selection",
    val cancelActionId: String? = null, val directory: String = "selected-artworks")

/** Optional generators.json in a STICKERS resource archive; all referenced bytes are manifest-hashed. */
@Serializable data class StickerGenerators(val schemaVersion: Int = 1, val items: List<VectorStickerGenerator> = emptyList())
@Serializable enum class StickerParameterKind { TEXT, COLOR, NUMBER, CHOICE }
@Serializable data class StickerParameter(val id: String, val label: String, val kind: StickerParameterKind,
    val default: String, val minimum: Double = 0.0, val maximum: Double = 100.0,
    val choices: List<String> = emptyList(), val maxLength: Int = 80)
/** SVG contains {{parameterId}} placeholders. Text is XML-escaped. font maps family names to archive fonts. */
@Serializable data class VectorStickerGenerator(val id: String, val name: String, val template: String,
    val width: Int = 1024, val height: Int = 512, val parameters: List<StickerParameter> = emptyList(),
    val fonts: Map<String, String> = emptyMap())
