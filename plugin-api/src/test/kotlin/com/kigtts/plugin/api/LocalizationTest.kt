package com.kigtts.plugin.api

import kotlin.test.*

class LocalizationTest {
    @Test fun localeFallbackAndArgumentsDoNotRecursivelyInterpretContent() {
        val strings = PluginStrings(mapOf("zh-CN" to mapOf("greeting" to "你好 {name}"), "zh" to mapOf("count" to "{number} 项"),
            "en" to mapOf("greeting" to "Hello {name}")))
        assertEquals("Hello {name}", strings.text("greeting", "en-US", mapOf("name" to "{name}")))
        assertEquals("3 项", strings.text("count", "zh-Hant-HK", mapOf("number" to "3")))
        assertEquals("你好 Alice", strings.text("greeting", "fr-FR", mapOf("name" to "Alice")))
        assertEquals("备用", strings.text("missing", "en", fallback = "备用"))
    }
    @Test fun localeMetadataRejectsPathsAndMissingFallback() {
        assertFailsWith<IllegalArgumentException> { PluginLocalization("zh-CN", listOf("../secret")) }
        assertFailsWith<IllegalArgumentException> { PluginLocalization("fr", listOf("en")) }
    }
}
