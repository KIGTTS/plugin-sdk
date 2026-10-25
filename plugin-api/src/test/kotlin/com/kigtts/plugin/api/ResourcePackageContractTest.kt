package com.kigtts.plugin.api

import kotlinx.serialization.encodeToString
import kotlin.test.*
import org.junit.Test

class ResourcePackageContractTest {
    @Test fun previewDimensionsRetainArbitraryAspectAndLegacyConstructors() {
        val image = PageImage(PluginResource("preview.svg"), "长图", 1, 100000)
        assertEquals(image, PluginJson.decodeFromString<PageImage>(PluginJson.encodeToString(image)))
        assertEquals(100000, image.copy(description = "新说明").height)
        assertNotNull(PageImage::class.java.getConstructor(PluginResource::class.java, String::class.java))
        assertNotNull(ManagementList::class.java.getConstructor(String::class.java, List::class.java, String::class.java,
            String::class.java, List::class.java, ManagementListStyle::class.java, Map::class.java, List::class.java, ManagementLoadMore::class.java))
        assertFailsWith<IllegalArgumentException> { PageImage(PluginResource("x"), width = 1) }
    }
    @Test fun artworkFramesAndGeneratorsRoundTrip() {
        val request = ArtworkSelectionRequest(ArtworkLibrary.PERSONAL_CARDS, multiple = true, selectedIds = listOf("one"))
        assertEquals(request, PluginJson.decodeFromString<ArtworkSelectionRequest>(PluginJson.encodeToString(request)))
        val frames = PersonalCardFramePackage(items = listOf(PersonalCardFrameResource("fine", "细线", "fine.svg")))
        assertEquals(frames, PluginJson.decodeFromString<PersonalCardFramePackage>(PluginJson.encodeToString(frames)))
        assertNotEquals(ResourceType.FRAME, ResourceType.CARD_FRAME)
        val generators = StickerGenerators(items = listOf(VectorStickerGenerator("word", "文字", "word.svg", parameters = listOf(
            StickerParameter("text", "文字", StickerParameterKind.TEXT, "你好")), fonts = mapOf("Art" to "art.ttf"))))
        assertEquals(generators, PluginJson.decodeFromString<StickerGenerators>(PluginJson.encodeToString(generators)))
    }
    @Test fun commandsAndDataRoundTrip() {
        val request: AppCommand = AppCommand.ResourcePackage(source = PluginResource("downloads/test.kigresource"), expectedPublisher = "a".repeat(64))
        assertEquals(request, PluginJson.decodeFromString<AppCommand>(PluginJson.encodeToString(request)))
        val content = ResourcePackageContent(frames = listOf(PolaroidFrameStyle("sample.frame", "纸白")))
        assertEquals(content, PluginJson.decodeFromString<ResourcePackageContent>(PluginJson.encodeToString(content)))
    }
}
