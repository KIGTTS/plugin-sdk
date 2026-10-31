@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "UNUSED_PARAMETER")
package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.jvm.internal.DefaultConstructorMarker

@Serializable enum class CardLayerKind { IMAGE, TEXT, PHOTO_INFO }
@Serializable enum class PhotoBackdropMode { GRAY, BLUR }
/** Decorations use the selected format's paper. Dimensions are fractions of its width. */
@Serializable data class PolaroidFrameStyle @JvmOverloads constructor(val id:String,val name:String,val version:Long=1,val color:Long?=null,
    val borderColor:Long?=null,val borderWidth:Double=0.0,val photoCornerRadius:Double=0.0,
    val overlayImageId:String?=null,val owner:String?=null,val format:PolaroidFormat=PolaroidFormat.LEGACY) {
    constructor(id:String?,name:String?,version:Long,color:Long?,borderColor:Long?,borderWidth:Double,photoCornerRadius:Double,
        overlayImageId:String?,owner:String?,mask:Int,marker:DefaultConstructorMarker?):this(requireNotNull(id),requireNotNull(name),
        if(mask and 4!=0)1 else version,if(mask and 8!=0)null else color,if(mask and 16!=0)null else borderColor,
        if(mask and 32!=0)0.0 else borderWidth,if(mask and 64!=0)0.0 else photoCornerRadius,
        if(mask and 128!=0)null else overlayImageId,if(mask and 256!=0)null else owner)
    @JvmName("copy") fun legacyCopy(id:String,name:String,version:Long,color:Long?,borderColor:Long?,borderWidth:Double,
        photoCornerRadius:Double,overlayImageId:String?,owner:String?)=copy(id=id,name=name,version=version,color=color,
        borderColor=borderColor,borderWidth=borderWidth,photoCornerRadius=photoCornerRadius,overlayImageId=overlayImageId,owner=owner)
    companion object {
        @JvmStatic @JvmName("copy\$default") fun legacyCopyDefault(value:PolaroidFrameStyle,id:String?,name:String?,version:Long,
            color:Long?,borderColor:Long?,borderWidth:Double,photoCornerRadius:Double,overlayImageId:String?,owner:String?,mask:Int,
            marker:Any?):PolaroidFrameStyle=value.legacyCopy(if(mask and 1!=0)value.id else requireNotNull(id),
            if(mask and 2!=0)value.name else requireNotNull(name),if(mask and 4!=0)value.version else version,
            if(mask and 8!=0)value.color else color,if(mask and 16!=0)value.borderColor else borderColor,
            if(mask and 32!=0)value.borderWidth else borderWidth,if(mask and 64!=0)value.photoCornerRadius else photoCornerRadius,
            if(mask and 128!=0)value.overlayImageId else overlayImageId,if(mask and 256!=0)value.owner else owner)
    }
}

/** Width uses the complete paper's width; text interprets it as its font-size fraction. */
@Serializable data class CardLayer @JvmOverloads constructor(val id:String=UUID.randomUUID().toString(),val kind:CardLayerKind,
    val imageId:String?=null,val text:String="",val x:Double=.5,val y:Double=.5,val width:Double=.25,
    val rotation:Double=0.0,val color:Long=0xFF202020,val flipX:Boolean=false,val flipY:Boolean=false) {
    // Preserve the exact API9 symbols used by already-installed Kotlin/Java source plugins.
    constructor(id:String?,kind:CardLayerKind?,imageId:String?,text:String?,x:Double,y:Double,width:Double,
        rotation:Double,color:Long,mask:Int,marker:DefaultConstructorMarker?):this(
        if(mask and 1!=0)UUID.randomUUID().toString() else requireNotNull(id),requireNotNull(kind),
        if(mask and 4!=0)null else imageId,if(mask and 8!=0)"" else requireNotNull(text),
        if(mask and 16!=0).5 else x,if(mask and 32!=0).5 else y,if(mask and 64!=0).25 else width,
        if(mask and 128!=0)0.0 else rotation,if(mask and 256!=0)0xFF202020 else color,false,false)
    @JvmName("copy") fun legacyCopy(id:String,kind:CardLayerKind,imageId:String?,text:String,x:Double,y:Double,
        width:Double,rotation:Double,color:Long)=copy(id=id,kind=kind,imageId=imageId,text=text,x=x,y=y,width=width,rotation=rotation,color=color)
    companion object {
        @JvmStatic @JvmName("copy\$default") fun legacyCopyDefault(value:CardLayer,id:String?,kind:CardLayerKind?,imageId:String?,text:String?,
            x:Double,y:Double,width:Double,rotation:Double,color:Long,mask:Int,marker:Any?):CardLayer=value.legacyCopy(
            if(mask and 1!=0)value.id else requireNotNull(id),if(mask and 2!=0)value.kind else requireNotNull(kind),
            if(mask and 4!=0)value.imageId else imageId,if(mask and 8!=0)value.text else requireNotNull(text),
            if(mask and 16!=0)value.x else x,if(mask and 32!=0)value.y else y,if(mask and 64!=0)value.width else width,
            if(mask and 128!=0)value.rotation else rotation,if(mask and 256!=0)value.color else color)
    }
}

/** Photo center is relative to its window. Zoom multiplies the centered Fit scale. */
@Serializable data class PolaroidCard constructor(val id:String=UUID.randomUUID().toString(),val title:String,val description:String="",
    val imageId:String?=null,val layers:List<CardLayer> = emptyList(),val frameColor:Long=DEFAULT_POLAROID_PAPER_COLOR,
    val photoX:Double=.5,val photoY:Double=.5,val photoZoom:Double=1.0,val photoBackdrop:PhotoBackdropMode=PhotoBackdropMode.GRAY,
    val frameStyleId:String="instax.square.classic",val originalImageId:String?=null,val photoStyle:PolaroidPhotoStyle?=null) {
    constructor(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode,frameStyleId:String,originalImageId:String?):this(
        id,title,description,imageId,layers,frameColor,photoX,photoY,photoZoom,photoBackdrop,frameStyleId,originalImageId,null)
    constructor(id:String?,title:String?,description:String?,imageId:String?,layers:List<CardLayer>?,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode?,frameStyleId:String?,originalImageId:String?,mask:Int,
        marker:DefaultConstructorMarker?):this(if(mask and 1!=0)UUID.randomUUID().toString() else requireNotNull(id),requireNotNull(title),
        if(mask and 4!=0)"" else requireNotNull(description),if(mask and 8!=0)null else imageId,
        if(mask and 16!=0)emptyList() else requireNotNull(layers),if(mask and 32!=0)DEFAULT_POLAROID_PAPER_COLOR else frameColor,
        if(mask and 64!=0).5 else photoX,if(mask and 128!=0).5 else photoY,if(mask and 256!=0)1.0 else photoZoom,
        if(mask and 512!=0)PhotoBackdropMode.GRAY else requireNotNull(photoBackdrop),
        if(mask and 1024!=0)"instax.square.classic" else requireNotNull(frameStyleId),if(mask and 2048!=0)null else originalImageId,null)
    constructor(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long):this(
        id,title,description,imageId,layers,frameColor,frameStyleId="classic",originalImageId=null)
    constructor(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode):this(id,title,description,imageId,layers,frameColor,
        photoX,photoY,photoZoom,photoBackdrop,"classic",null)
    constructor(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode,frameStyleId:String):this(id,title,description,imageId,
        layers,frameColor,photoX,photoY,photoZoom,photoBackdrop,frameStyleId,null)
    constructor(id:String?,title:String?,description:String?,imageId:String?,layers:List<CardLayer>?,frameColor:Long,
        mask:Int,marker:DefaultConstructorMarker?):this(if(mask and 1!=0)UUID.randomUUID().toString() else requireNotNull(id),requireNotNull(title),
        if(mask and 4!=0)"" else requireNotNull(description),if(mask and 8!=0)null else imageId,
        if(mask and 16!=0)emptyList() else requireNotNull(layers),if(mask and 32!=0)DEFAULT_POLAROID_PAPER_COLOR else frameColor,frameStyleId="classic")
    constructor(id:String?,title:String?,description:String?,imageId:String?,layers:List<CardLayer>?,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode?,mask:Int,marker:DefaultConstructorMarker?):this(
        if(mask and 1!=0)UUID.randomUUID().toString() else requireNotNull(id),requireNotNull(title),
        if(mask and 4!=0)"" else requireNotNull(description),if(mask and 8!=0)null else imageId,
        if(mask and 16!=0)emptyList() else requireNotNull(layers),if(mask and 32!=0)DEFAULT_POLAROID_PAPER_COLOR else frameColor,
        if(mask and 64!=0).5 else photoX,if(mask and 128!=0).5 else photoY,if(mask and 256!=0)1.0 else photoZoom,
        if(mask and 512!=0)PhotoBackdropMode.GRAY else requireNotNull(photoBackdrop),frameStyleId="classic")
    constructor(id:String?,title:String?,description:String?,imageId:String?,layers:List<CardLayer>?,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode?,frameStyleId:String?,mask:Int,
        marker:DefaultConstructorMarker?):this(if(mask and 1!=0)UUID.randomUUID().toString() else requireNotNull(id),requireNotNull(title),
        if(mask and 4!=0)"" else requireNotNull(description),if(mask and 8!=0)null else imageId,
        if(mask and 16!=0)emptyList() else requireNotNull(layers),if(mask and 32!=0)DEFAULT_POLAROID_PAPER_COLOR else frameColor,
        if(mask and 64!=0).5 else photoX,if(mask and 128!=0).5 else photoY,if(mask and 256!=0)1.0 else photoZoom,
        if(mask and 512!=0)PhotoBackdropMode.GRAY else requireNotNull(photoBackdrop),if(mask and 1024!=0)"classic" else requireNotNull(frameStyleId))
    @JvmName("copy") fun legacyCopy(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long)=
        copy(id=id,title=title,description=description,imageId=imageId,layers=layers,frameColor=frameColor)
    @JvmName("copy") fun legacyPhotoCopy(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode)=copy(id=id,title=title,description=description,
        imageId=imageId,layers=layers,frameColor=frameColor,photoX=photoX,photoY=photoY,photoZoom=photoZoom,photoBackdrop=photoBackdrop)
    @JvmName("copy") fun legacyFrameCopy(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode,frameStyleId:String)=copy(id=id,title=title,
        description=description,imageId=imageId,layers=layers,frameColor=frameColor,photoX=photoX,photoY=photoY,photoZoom=photoZoom,
        photoBackdrop=photoBackdrop,frameStyleId=frameStyleId)
    @JvmName("copy") fun legacyOriginalCopy(id:String,title:String,description:String,imageId:String?,layers:List<CardLayer>,frameColor:Long,
        photoX:Double,photoY:Double,photoZoom:Double,photoBackdrop:PhotoBackdropMode,frameStyleId:String,originalImageId:String?)=copy(id=id,title=title,
        description=description,imageId=imageId,layers=layers,frameColor=frameColor,photoX=photoX,photoY=photoY,photoZoom=photoZoom,
        photoBackdrop=photoBackdrop,frameStyleId=frameStyleId,originalImageId=originalImageId)
    companion object {
        @JvmStatic @JvmName("copy\$default") fun legacyOriginalCopyDefault(value:PolaroidCard,id:String?,title:String?,description:String?,
            imageId:String?,layers:List<CardLayer>?,frameColor:Long,photoX:Double,photoY:Double,photoZoom:Double,
            photoBackdrop:PhotoBackdropMode?,frameStyleId:String?,originalImageId:String?,mask:Int,marker:Any?):PolaroidCard=value.legacyOriginalCopy(
            if(mask and 1!=0)value.id else requireNotNull(id),if(mask and 2!=0)value.title else requireNotNull(title),
            if(mask and 4!=0)value.description else requireNotNull(description),if(mask and 8!=0)value.imageId else imageId,
            if(mask and 16!=0)value.layers else requireNotNull(layers),if(mask and 32!=0)value.frameColor else frameColor,
            if(mask and 64!=0)value.photoX else photoX,if(mask and 128!=0)value.photoY else photoY,
            if(mask and 256!=0)value.photoZoom else photoZoom,if(mask and 512!=0)value.photoBackdrop else requireNotNull(photoBackdrop),
            if(mask and 1024!=0)value.frameStyleId else requireNotNull(frameStyleId),if(mask and 2048!=0)value.originalImageId else originalImageId)
        @JvmStatic @JvmName("copy\$default") fun legacyCopyDefault(value:PolaroidCard,id:String?,title:String?,description:String?,
            imageId:String?,layers:List<CardLayer>?,frameColor:Long,mask:Int,marker:Any?):PolaroidCard=value.legacyCopy(
            if(mask and 1!=0)value.id else requireNotNull(id),if(mask and 2!=0)value.title else requireNotNull(title),
            if(mask and 4!=0)value.description else requireNotNull(description),if(mask and 8!=0)value.imageId else imageId,
            if(mask and 16!=0)value.layers else requireNotNull(layers),if(mask and 32!=0)value.frameColor else frameColor)
        @JvmStatic @JvmName("copy\$default") fun legacyPhotoCopyDefault(value:PolaroidCard,id:String?,title:String?,description:String?,
            imageId:String?,layers:List<CardLayer>?,frameColor:Long,photoX:Double,photoY:Double,photoZoom:Double,
            photoBackdrop:PhotoBackdropMode?,mask:Int,marker:Any?):PolaroidCard=value.legacyPhotoCopy(
            if(mask and 1!=0)value.id else requireNotNull(id),if(mask and 2!=0)value.title else requireNotNull(title),
            if(mask and 4!=0)value.description else requireNotNull(description),if(mask and 8!=0)value.imageId else imageId,
            if(mask and 16!=0)value.layers else requireNotNull(layers),if(mask and 32!=0)value.frameColor else frameColor,
            if(mask and 64!=0)value.photoX else photoX,if(mask and 128!=0)value.photoY else photoY,
            if(mask and 256!=0)value.photoZoom else photoZoom,if(mask and 512!=0)value.photoBackdrop else requireNotNull(photoBackdrop))
        @JvmStatic @JvmName("copy\$default") fun legacyFrameCopyDefault(value:PolaroidCard,id:String?,title:String?,description:String?,
            imageId:String?,layers:List<CardLayer>?,frameColor:Long,photoX:Double,photoY:Double,photoZoom:Double,
            photoBackdrop:PhotoBackdropMode?,frameStyleId:String?,mask:Int,marker:Any?):PolaroidCard=value.legacyFrameCopy(
            if(mask and 1!=0)value.id else requireNotNull(id),if(mask and 2!=0)value.title else requireNotNull(title),
            if(mask and 4!=0)value.description else requireNotNull(description),if(mask and 8!=0)value.imageId else imageId,
            if(mask and 16!=0)value.layers else requireNotNull(layers),if(mask and 32!=0)value.frameColor else frameColor,
            if(mask and 64!=0)value.photoX else photoX,if(mask and 128!=0)value.photoY else photoY,
            if(mask and 256!=0)value.photoZoom else photoZoom,if(mask and 512!=0)value.photoBackdrop else requireNotNull(photoBackdrop),
            if(mask and 1024!=0)value.frameStyleId else requireNotNull(frameStyleId))
    }
}
