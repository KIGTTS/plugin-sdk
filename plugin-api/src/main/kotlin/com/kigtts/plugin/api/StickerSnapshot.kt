@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "UNUSED_PARAMETER")
package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import kotlin.jvm.internal.DefaultConstructorMarker

@Serializable data class StickerSnapshot constructor(val revision:Long,val libraries:List<StickerLibrary> = emptyList(),
    val items:List<StickerItem> = emptyList(),val images:List<StickerImage> = emptyList(),val cards:List<PolaroidCard> = emptyList(),
    val offset:Int=0,val total:Int=0,val nextOffset:Int?=null,val albums:List<PolaroidAlbum> = emptyList()) {
    // Retain the API9 symbols linked by already-installed native source plugins.
    constructor(revision:Long,libraries:List<StickerLibrary>,items:List<StickerItem>,images:List<StickerImage>,cards:List<PolaroidCard>,
        offset:Int,total:Int,nextOffset:Int?):this(revision,libraries,items,images,cards,offset,total,nextOffset,emptyList())
    constructor(revision:Long,libraries:List<StickerLibrary>?,items:List<StickerItem>?,images:List<StickerImage>?,cards:List<PolaroidCard>?,
        offset:Int,total:Int,nextOffset:Int?,mask:Int,marker:DefaultConstructorMarker?):this(revision,
        if(mask and 2!=0)emptyList() else requireNotNull(libraries),if(mask and 4!=0)emptyList() else requireNotNull(items),
        if(mask and 8!=0)emptyList() else requireNotNull(images),if(mask and 16!=0)emptyList() else requireNotNull(cards),
        if(mask and 32!=0)0 else offset,if(mask and 64!=0)0 else total,if(mask and 128!=0)null else nextOffset,emptyList())
    @JvmName("copy") fun legacyCopy(revision:Long,libraries:List<StickerLibrary>,items:List<StickerItem>,images:List<StickerImage>,
        cards:List<PolaroidCard>,offset:Int,total:Int,nextOffset:Int?)=copy(revision=revision,libraries=libraries,items=items,
        images=images,cards=cards,offset=offset,total=total,nextOffset=nextOffset)
    companion object {
        @JvmStatic @JvmName("copy\$default") fun legacyCopyDefault(value:StickerSnapshot,revision:Long,libraries:List<StickerLibrary>?,
            items:List<StickerItem>?,images:List<StickerImage>?,cards:List<PolaroidCard>?,offset:Int,total:Int,nextOffset:Int?,mask:Int,
            marker:Any?):StickerSnapshot=value.legacyCopy(if(mask and 1!=0)value.revision else revision,
            if(mask and 2!=0)value.libraries else requireNotNull(libraries),if(mask and 4!=0)value.items else requireNotNull(items),
            if(mask and 8!=0)value.images else requireNotNull(images),if(mask and 16!=0)value.cards else requireNotNull(cards),
            if(mask and 32!=0)value.offset else offset,if(mask and 64!=0)value.total else total,if(mask and 128!=0)value.nextOffset else nextOffset)
    }
}
