package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.Locale

/** UI language metadata; independent of the language accepted by an ASR/TTS model. */
@Serializable data class PluginLocalization(val defaultLocale: String = "zh-CN",
    val supportedLocales: List<String> = listOf(defaultLocale)) {
    init {
        require(supportedLocales.size in 1..64 && defaultLocale in supportedLocales)
        supportedLocales.forEach { require(it.matches(Regex("[a-zA-Z]{2,8}(-[a-zA-Z0-9]{1,8}){0,5}"))) }
    }
}

/** Small key-based catalogs. Native plural/ICU formats can be introduced without replacing the wire labels. */
class PluginStrings(private val catalogs: Map<String, Map<String, String>>, private val defaultLocale: String = "zh-CN") {
    fun text(key: String, locale: String, arguments: Map<String, String> = emptyMap(), fallback: String = key): String {
        val normalized = Locale.forLanguageTag(locale).toLanguageTag()
        val candidates = buildList {
            var tag = normalized
            while (tag.isNotBlank() && tag != "und") { add(tag); tag = tag.substringBeforeLast('-', "") }
            add(defaultLocale)
        }.distinct()
        val pattern = candidates.firstNotNullOfOrNull { tag -> catalogs.entries.firstOrNull { it.key.equals(tag, true) }?.value?.get(key) } ?: fallback
        return Regex("\\{([a-zA-Z][a-zA-Z0-9_.-]*)}").replace(pattern) { match -> arguments[match.groupValues[1]] ?: match.value }
    }
    companion object {
        fun fromAssets(assets: File, defaultLocale: String = "zh-CN"): PluginStrings {
            val root = assets.canonicalFile
            val directory = File(root, "i18n").canonicalFile
            require(directory.toPath().startsWith(root.toPath()))
            val files = directory.listFiles().orEmpty().filter { it.isFile && it.extension == "json" }
            require(files.size <= 64)
            val catalogs = files.associate { file ->
                require(file.canonicalFile.toPath().startsWith(directory.toPath()) && !java.nio.file.Files.isSymbolicLink(file.toPath()))
                require(file.length() <= 128 * 1024 && file.nameWithoutExtension.matches(Regex("[a-zA-Z]{2,8}(-[a-zA-Z0-9]{1,8}){0,5}")))
                val values = PluginJson.parseToJsonElement(file.readText(Charsets.UTF_8)).jsonObject
                require(values.size <= 2000)
                file.nameWithoutExtension to values.mapValues { (key, value) ->
                    require(key.length in 1..160); value.jsonPrimitive.content.also { require(it.length <= 8000) }
                }
            }
            return PluginStrings(catalogs, defaultLocale)
        }
    }
}
