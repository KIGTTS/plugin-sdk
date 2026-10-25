package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** API29 data-only KPM archives. No executable entrypoint or plugin lifecycle. */
@Serializable enum class ResourceType(val label: String, val symbol: String) {
    QUICK_TEXT("快捷文本", "text_snippet"), PHOTOS("照片集", "photo_library"), FILTER("滤镜", "filter"),
    VOICE("语音包", "record_voice_over"), FRAME("照片边框", "crop_square"), STICKERS("贴纸包", "emoji_emotions")
}
@Serializable data class ResourcePackageManifest(val schemaVersion: Int = 1, val id: String, val name: String,
    val version: String, val versionCode: Int, val resourceType: ResourceType,
    val files: Map<String, String>, val apiVersion: Int = 29, val minSdk: Int = 26,
    val dependencies: Map<String, String> = emptyMap(), val content: String = "content.json")
@Serializable data class ResourceFile(val path: String, val name: String = "")
@Serializable data class ResourcePackageContent(val schemaVersion: Int = 1, val payload: String? = null,
    val filters: List<ResourceFile> = emptyList(), val snapshot: StickerSnapshot? = null,
    val frames: List<PolaroidFrameStyle> = emptyList(), val imageFiles: Map<String, String> = emptyMap())
