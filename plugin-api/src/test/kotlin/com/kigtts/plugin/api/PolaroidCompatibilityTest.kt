package com.kigtts.plugin.api

import kotlin.test.*

class PolaroidCompatibilityTest {
    private val marker=Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    @Test fun oldApi9CardConstructorAndKotlinDefaultConstructorSymbolsStillLink() {
        val old=PolaroidCard::class.java.getConstructor(String::class.java,String::class.java,String::class.java,String::class.java,List::class.java,Long::class.javaPrimitiveType)
            .newInstance("old","名片","说明",null,emptyList<CardLayer>(),0xFF112233L)
        assertEquals(PhotoBackdropMode.GRAY,old.photoBackdrop);assertEquals(1.0,old.photoZoom)
        val defaults=PolaroidCard::class.java.getConstructor(String::class.java,String::class.java,String::class.java,String::class.java,List::class.java,
            Long::class.javaPrimitiveType,Int::class.javaPrimitiveType,marker).newInstance(null,"旧默认构造",null,null,null,0L,61,null)
        assertEquals("旧默认构造",defaults.title);assertTrue(defaults.id.isNotBlank());assertEquals(DEFAULT_POLAROID_PAPER_COLOR,defaults.frameColor)
    }
    @Test fun oldCardCopyAndCopyDefaultPreserveNewPoseInsteadOfResettingIt() {
        val original=PolaroidCard(title="原名片",photoX=.25,photoY=.75,photoZoom=3.0,photoBackdrop=PhotoBackdropMode.BLUR)
        val copy=PolaroidCard::class.java.getMethod("copy",String::class.java,String::class.java,String::class.java,String::class.java,List::class.java,Long::class.javaPrimitiveType)
            .invoke(original,original.id,"旧copy","说明",null,emptyList<CardLayer>(),original.frameColor) as PolaroidCard
        assertEquals(.25,copy.photoX);assertEquals(3.0,copy.photoZoom)
        val default=PolaroidCard::class.java.getMethod("copy\$default",PolaroidCard::class.java,String::class.java,String::class.java,String::class.java,String::class.java,
            List::class.java,Long::class.javaPrimitiveType,Int::class.javaPrimitiveType,Any::class.java)
            .invoke(null,original,null,"旧默认copy",null,null,null,0L,61,null) as PolaroidCard
        assertEquals("旧默认copy",default.title);assertEquals(.75,default.photoY);assertEquals(PhotoBackdropMode.BLUR,default.photoBackdrop)
    }
    @Test fun oldLayerDefaultConstructorAndCopiesKeepMirroring() {
        val type=CardLayer::class.java;val primitives=arrayOf(String::class.java,CardLayerKind::class.java,String::class.java,String::class.java,
            Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,Long::class.javaPrimitiveType!!)
        val defaults=type.getConstructor(*primitives,Int::class.javaPrimitiveType!!,marker)
            .newInstance(null,CardLayerKind.TEXT,null,null,0.0,0.0,0.0,0.0,0L,509,null)
        assertEquals(.5,defaults.x);assertEquals(.25,defaults.width);assertFalse(defaults.flipX)
        val original=CardLayer(kind=CardLayerKind.TEXT,text="镜像",flipX=true,flipY=true)
        val copied=type.getMethod("copy\$default",type,*primitives,Int::class.javaPrimitiveType!!,Any::class.java)
            .invoke(null,original,null,null,null,"新文字",0.0,0.0,0.0,0.0,0L,503,null) as CardLayer
        assertEquals("新文字",copied.text);assertTrue(copied.flipX);assertTrue(copied.flipY)
    }
    @Test fun oldJsonDefaultsAndNewPoseMirrorsRoundTripAcrossTypedProtocol() {
        val legacy=PluginJson.decodeFromString<PolaroidCard>("""{"id":"old","title":"老名片"}""")
        assertEquals(.5,legacy.photoX);assertEquals(PhotoBackdropMode.GRAY,legacy.photoBackdrop)
        val value=legacy.copy(photoX=.2,photoY=.8,photoZoom=2.5,photoBackdrop=PhotoBackdropMode.BLUR,
            layers=listOf(CardLayer(kind=CardLayerKind.TEXT,text="🙂",flipX=true,flipY=true)))
        assertEquals(value,PluginJson.decodeFromString<PolaroidCard>(PluginJson.encodeToString(value)))
    }
    @Test fun intermediateTenFieldConstructorAndCopyKeepBrandFrameWhenAddingEleventhField() {
        val signature=arrayOf(String::class.java,String::class.java,String::class.java,String::class.java,List::class.java,Long::class.javaPrimitiveType!!,
            Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,PhotoBackdropMode::class.java)
        val old=PolaroidCard::class.java.getConstructor(*signature,Int::class.javaPrimitiveType!!,marker)
            .newInstance(null,"兼容构造",null,null,null,0L,0.0,0.0,0.0,null,1021,null)
        assertEquals("classic",old.frameStyleId);assertEquals(.5,old.photoX);assertEquals(DEFAULT_POLAROID_PAPER_COLOR,old.frameColor)
        val branded=old.copy(frameStyleId="brand.example",photoZoom=3.0)
        val copied=PolaroidCard::class.java.getMethod("copy\$default",PolaroidCard::class.java,*signature,Int::class.javaPrimitiveType!!,Any::class.java)
            .invoke(null,branded,null,"兼容copy",null,null,null,0L,0.0,0.0,0.0,null,1021,null) as PolaroidCard
        assertEquals("兼容copy",copied.title);assertEquals("brand.example",copied.frameStyleId);assertEquals(3.0,copied.photoZoom)
    }
    @Test fun nativePaperAndPhotoDimensionsUseSharedPhysicalRatiosWithoutInventingLegacyMillimeters() {
        val sizes=listOf(Triple(PolaroidFormat.MINI,54.0,46.0),Triple(PolaroidFormat.SQUARE,72.0,62.0),Triple(PolaroidFormat.WIDE,108.0,99.0))
        for((format,paperWidth,photoWidth) in sizes) {
            val layout=format.geometry;assertEquals(paperWidth,layout.paperWidth);assertEquals(86.0,layout.paperHeight)
            assertEquals(photoWidth,layout.photoWidth);assertEquals(62.0,layout.photoHeight)
            assertEquals(photoWidth/62.0,layout.photoAspectRatio);assertEquals(paperWidth/86.0,layout.aspectRatio)
            assertEquals((paperWidth-photoWidth)/2,layout.photoLeft);assertEquals(6.0,layout.photoTop);assertEquals(18.0,layout.paperHeight-layout.photoBottom)
            assertTrue(layout.millimeterSizing);assertTrue(layout.descriptionY+layout.descriptionSize*1.2*3<layout.paperHeight)
        }
        assertFalse(PolaroidFormat.LEGACY.geometry.millimeterSizing);assertEquals(.8,PolaroidFormat.LEGACY.geometry.aspectRatio)
    }
    @Test fun elevenFieldCopyKeepsOriginalResourceAndPreviousFrameConstructorDefaultsLegacy() {
        val signature=arrayOf(String::class.java,String::class.java,String::class.java,String::class.java,List::class.java,Long::class.javaPrimitiveType!!,
            Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,PhotoBackdropMode::class.java,String::class.java)
        val constructed=PolaroidCard::class.java.getConstructor(*signature,Int::class.javaPrimitiveType!!,marker)
            .newInstance(null,"旧十一字段",null,null,null,0L,0.0,0.0,0.0,null,null,2045,null)
        assertEquals("classic",constructed.frameStyleId);assertNull(constructed.originalImageId);assertEquals(DEFAULT_POLAROID_PAPER_COLOR,constructed.frameColor)
        val original=constructed.copy(originalImageId="raw-photo",frameStyleId="instax.square.classic")
        val copied=PolaroidCard::class.java.getMethod("copy\$default",PolaroidCard::class.java,*signature,Int::class.javaPrimitiveType!!,Any::class.java)
            .invoke(null,original,null,"旧copy保持原图",null,null,null,0L,0.0,0.0,0.0,null,null,2045,null) as PolaroidCard
        assertEquals("raw-photo",copied.originalImageId);assertEquals(original.frameStyleId,copied.frameStyleId)
    }
    @Test fun previousNineFieldFrameDefaultConstructorAndCopyPreserveNewFormat() {
        val signature=arrayOf(String::class.java,String::class.java,Long::class.javaPrimitiveType!!,Long::class.javaObjectType,
            Long::class.javaObjectType,Double::class.javaPrimitiveType!!,Double::class.javaPrimitiveType!!,String::class.java,String::class.java)
        val old=PolaroidFrameStyle::class.java.getConstructor(*signature,Int::class.javaPrimitiveType!!,marker)
            .newInstance("brand.style","旧边框",0L,null,null,0.0,0.0,null,null,508,null)
        assertEquals(PolaroidFormat.LEGACY,old.format)
        val modern=old.copy(format=PolaroidFormat.WIDE)
        val copy=PolaroidFrameStyle::class.java.getMethod("copy\$default",PolaroidFrameStyle::class.java,*signature,Int::class.javaPrimitiveType!!,Any::class.java)
            .invoke(null,modern,null,"旧copy",0L,null,null,0.0,0.0,null,null,509,null) as PolaroidFrameStyle
        assertEquals(PolaroidFormat.WIDE,copy.format);assertEquals("旧copy",copy.name)
    }
    @Test fun pureWhiteDefaultsAndLegacyCreamNormalizationPreserveCustomPaperAndBrandResources() {
        assertEquals(0xFFFFFFFFL,PolaroidCard(title="新纸面").frameColor)
        assertEquals(0xFFFFFFFFL,PluginJson.decodeFromString<PolaroidCard>("""{"id":"old","title":"未保存颜色"}""").frameColor)
        val cream=0xFFF7F2E8L
        for(style in listOf("classic","instax.mini.classic","instax.square.classic","instax.wide.classic")) {
            val legacy=PolaroidCard(title="旧默认",frameStyleId=style,frameColor=cream)
            assertEquals(0xFFFFFFFFL,legacy.normalizeLegacyPaper().frameColor)
            assertEquals(0xFFFFFFFFL,polaroidPaperColor(legacy))
            assertEquals(0xFF123456L,polaroidPaperColor(legacy.copy(frameColor=0xFF123456)))
        }
        val branded=PolaroidCard(title="品牌",frameStyleId="brand.paper",frameColor=cream)
        val frame=PolaroidFrameStyle("brand.paper","品牌",owner="brand",overlayImageId="brand-resource")
        assertEquals(branded,branded.normalizeLegacyPaper());assertEquals(cream,polaroidPaperColor(branded,frame))
        assertEquals(0xFF112233L,polaroidPaperColor(branded,frame.copy(color=0xFF112233)))
        assertEquals("brand-resource",frame.overlayImageId)
    }
}
