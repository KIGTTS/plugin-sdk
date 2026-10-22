package com.kigtts.plugin.api

import kotlin.test.*

class StickerSnapshotCompatibilityTest {
    private val marker=Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    private val oldSignature=arrayOf(Long::class.javaPrimitiveType!!,List::class.java,List::class.java,List::class.java,List::class.java,
        Int::class.javaPrimitiveType!!,Int::class.javaPrimitiveType!!,Int::class.javaObjectType)

    @Test fun oldFullAndKotlinDefaultSnapshotConstructorsKeepTheirExactApi9Signatures() {
        val type=StickerSnapshot::class.java
        val full=type.getConstructor(*oldSignature).newInstance(7L,emptyList<StickerLibrary>(),emptyList<StickerItem>(),
            emptyList<StickerImage>(),listOf(PolaroidCard(title="旧插件")),2,9,4)
        assertEquals(7L,full.revision);assertEquals(2,full.offset);assertEquals(4,full.nextOffset);assertTrue(full.albums.isEmpty())
        val defaults=type.getConstructor(*oldSignature,Int::class.javaPrimitiveType!!,marker)
            .newInstance(8L,null,null,null,null,0,0,null,254,null)
        assertEquals(8L,defaults.revision);assertTrue(defaults.cards.isEmpty());assertTrue(defaults.albums.isEmpty());assertNull(defaults.nextOffset)
    }
    @Test fun oldCopyAndCopyDefaultPreserveAlbumsWhenUpdatingTheOtherSnapshotFields() {
        val card=PolaroidCard(title="原图",originalImageId="original")
        val album=PolaroidAlbum(DEFAULT_POLAROID_ALBUM_ID,"默认相册",listOf(card.id))
        val value=StickerSnapshot(7,cards=listOf(card),albums=listOf(album))
        val type=StickerSnapshot::class.java
        val copied=type.getMethod("copy",*oldSignature).invoke(value,8L,value.libraries,value.items,value.images,value.cards,0,1,null) as StickerSnapshot
        assertEquals(listOf(album),copied.albums);assertEquals("original",copied.cards.single().originalImageId)
        val default=type.getMethod("copy\$default",type,*oldSignature,Int::class.javaPrimitiveType!!,Any::class.java)
            .invoke(null,value,9L,null,null,null,null,0,0,null,254,null) as StickerSnapshot
        assertEquals(9L,default.revision);assertEquals(value.albums,default.albums);assertEquals(value.cards,default.cards)
    }
    @Test fun oldJsonStillReadsAndAlbumsCommandsEditsAndFullCardsRoundTrip() {
        assertTrue(PluginJson.decodeFromString<StickerSnapshot>("""{"revision":1,"cards":[]}""").albums.isEmpty())
        val card=PolaroidCard(title="照片 😀",imageId="display",originalImageId="original",photoX=.3,photoZoom=2.0,
            frameStyleId="instax.wide.classic",layers=listOf(CardLayer(kind=CardLayerKind.TEXT,text="文字",flipX=true)))
        val album=PolaroidAlbum("travel","旅途",listOf(card.id))
        val value=StickerSnapshot(3,cards=listOf(card),albums=listOf(album))
        assertEquals(value,PluginJson.decodeFromString<StickerSnapshot>(PluginJson.encodeToString(value)))
        val commands=listOf<StickerCommand>(StickerCommand.QueryAlbums(1,10,3),StickerCommand.QueryAlbumCards("travel",2,5,3),
            StickerCommand.Prepare("album-transaction",3,listOf(StickerEdit.PutAlbum("travel","旅途"),
                StickerEdit.MoveAlbumCards("travel",listOf(card.id)),StickerEdit.OrderAlbumCards("travel",listOf(card.id)),
                StickerEdit.OrderAlbums(listOf(DEFAULT_POLAROID_ALBUM_ID,"travel")),StickerEdit.RemoveAlbums(listOf("travel")))))
        commands.forEach {assertEquals(it,PluginJson.decodeFromString<StickerCommand>(PluginJson.encodeToString<StickerCommand>(it)))}
        val reply:StickerReply=StickerReply.Albums(3,listOf(album),1)
        assertEquals(reply,PluginJson.decodeFromString<StickerReply>(PluginJson.encodeToString<StickerReply>(reply)))
    }
}
