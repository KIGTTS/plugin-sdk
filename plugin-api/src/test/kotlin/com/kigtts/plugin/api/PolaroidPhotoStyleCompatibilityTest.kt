package com.kigtts.plugin.api

import kotlin.test.*

class PolaroidPhotoStyleCompatibilityTest {
    @Test fun legacyJsonStaysBakedAndNewStyleRoundTrips() {
        val legacy = PluginJson.decodeFromString<PolaroidCard>("""{"id":"old","title":"旧照片","imageId":"baked"}""")
        assertNull(legacy.photoStyle)
        val value = legacy.copy(photoStyle = PolaroidPhotoStyle("BW", .35f, grain = .2f, lutImageId = "atlas", lutSize = 17))
        assertEquals(value, PluginJson.decodeFromString<PolaroidCard>(PluginJson.encodeToString(value)))
    }
    @Test fun oldTwelveFieldDefaultConstructorAndCopyKeepTheStyle() {
        val type = PolaroidCard::class.java
        val signature = arrayOf(String::class.java, String::class.java, String::class.java, String::class.java, List::class.java,
            Long::class.javaPrimitiveType!!, Double::class.javaPrimitiveType!!, Double::class.javaPrimitiveType!!,
            Double::class.javaPrimitiveType!!, PhotoBackdropMode::class.java, String::class.java, String::class.java)
        val marker = Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
        val old = type.getConstructor(*signature, Int::class.javaPrimitiveType!!, marker)
            .newInstance(null, "兼容", null, null, null, 0L, 0.0, 0.0, 0.0, null, null, null, 4093, null)
        assertNull(old.photoStyle); assertEquals("instax.square.classic", old.frameStyleId)
        val styled = old.copy(photoStyle = PolaroidPhotoStyle("WARM", .7f))
        val copied = type.getMethod("copy\$default", type, *signature, Int::class.javaPrimitiveType!!, Any::class.java)
            .invoke(null, styled, null, "旧插件改标题", null, null, null, 0L, 0.0, 0.0, 0.0, null, null, null, 4093, null) as PolaroidCard
        assertEquals(styled.photoStyle, copied.photoStyle); assertEquals("旧插件改标题", copied.title)
    }
}
