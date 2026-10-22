package com.kigtts.plugin.api

import java.io.File

/** Paths are scoped IO contracts, not a security sandbox for native code. */
interface PluginEnvironment {
    val locale: String get() = java.util.Locale.getDefault().toLanguageTag()
    val appBroker: AppBroker? get() = null
    val stickerBroker: StickerBroker? get() = null
    val quickTextBroker: QuickTextBroker? get() = null
    val speechBroker: SpeechBroker? get() = null
    val recognitionBroker: RecognitionBroker? get() = null
    val storage: PluginStorage
    /** Immutable resources shipped in the installed package. */
    val assets: File
    /** HTTPS only. The result and all partial files remain under storage.root. */
    suspend fun download(url: String, relativePath: String, sha256: String? = null): File
}

class PluginStorage(val root: File) {
    init { require(root.mkdirs() || root.isDirectory) }
    fun file(relativePath: String): File {
        require(relativePath.isNotBlank() && !relativePath.contains('\\') && !relativePath.contains(':') && !relativePath.contains('\u0000')) { "Use relative slash-separated paths" }
        require(!File(relativePath).isAbsolute && relativePath.split('/').none { it == ".." || it.isEmpty() })
        val target = File(root, relativePath).canonicalFile
        require(target.toPath().startsWith(root.canonicalFile.toPath()) && target != root.canonicalFile) { "Path escapes plugin data" }
        return target
    }
    fun directory(relativePath: String): File = file(relativePath).also { require(it.mkdirs() || it.isDirectory) }
    val models get() = directory("models")
    val downloads get() = directory("downloads")
    val temporary get() = directory("tmp")
    val configuration get() = directory("config")
}
