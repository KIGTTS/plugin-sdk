package com.kigtts.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/** Shared galleries and polaroid designs. Resource paths are never part of a catalog DTO. */
interface StickerBroker {
    suspend fun request(command: StickerCommand): StickerReply
    fun changes(afterRevision: Long = -1): Flow<StickerChange>
}

@Serializable data class StickerImage(val id:String,val mime:String,val bytes:Long,val width:Int,val height:Int,val version:Long)
@Serializable data class StickerLibrary(val id:String=UUID.randomUUID().toString(),val name:String)
@Serializable data class StickerItem(val id:String=UUID.randomUUID().toString(),val libraryId:String,val name:String,val imageId:String)
@Serializable data class StickerChange(val revision:Long,val decisionsVersion:Long=0)
@Serializable data class StickerPreview(val token:String,val revision:Long,val titles:List<String>,val deleted:Int,val requiresConfirmation:Boolean)
@Serializable enum class StickerScope { ALL, LIBRARIES, ITEMS, IMAGES, CARDS }
@Serializable enum class StickerUiTarget { CARDS, LIBRARY, EDIT_CARD, REVIEW }
@Serializable enum class StickerError { PERMISSION_DENIED, INVALID, NOT_FOUND, CONFLICT, BUSY, CONFIRMATION_REQUIRED, HOST_UNAVAILABLE, TOO_LARGE }

@Serializable sealed interface StickerEdit {
    @Serializable @SerialName("library.put") data class PutLibrary(val library:StickerLibrary):StickerEdit
    @Serializable @SerialName("item.put") data class PutItem(val item:StickerItem):StickerEdit
    @Serializable @SerialName("card.put") data class PutCard(val card:PolaroidCard):StickerEdit
    @Serializable @SerialName("frame.put") data class PutFrameStyle(val style:PolaroidFrameStyle):StickerEdit
    @Serializable @SerialName("frame.remove") data class RemoveFrameStyles(val ids:List<String>):StickerEdit
    @Serializable @SerialName("library.remove") data class RemoveLibraries(val ids:List<String>,val moveTo:String?=null,val deleteItems:Boolean=false):StickerEdit
    @Serializable @SerialName("item.remove") data class RemoveItems(val ids:List<String>):StickerEdit
    @Serializable @SerialName("card.remove") data class RemoveCards(val ids:List<String>):StickerEdit
    /** Referenced images cannot be removed until their items/cards/layers are updated in the same batch. */
    @Serializable @SerialName("image.remove") data class RemoveImages(val ids:List<String>):StickerEdit
    @Serializable @SerialName("library.order") data class OrderLibraries(val ids:List<String>):StickerEdit
    @Serializable @SerialName("item.order") data class OrderItems(val libraryId:String,val ids:List<String>):StickerEdit
    @Serializable @SerialName("card.order") data class OrderCards(val ids:List<String>):StickerEdit
    /** Metadata only; album membership/order is changed explicitly by MoveAlbumCards/OrderAlbumCards. */
    @Serializable @SerialName("album.put") data class PutAlbum(val id:String,val name:String):StickerEdit
    /** Photos are retained and appended to the protected default album. */
    @Serializable @SerialName("album.remove") data class RemoveAlbums(val ids:List<String>):StickerEdit
    @Serializable @SerialName("album.move-cards") data class MoveAlbumCards(val albumId:String,val cardIds:List<String>):StickerEdit
    @Serializable @SerialName("album.order") data class OrderAlbums(val ids:List<String>):StickerEdit
    @Serializable @SerialName("album.cards-order") data class OrderAlbumCards(val albumId:String,val cardIds:List<String>):StickerEdit
}

@Serializable sealed interface StickerCommand {
    @Serializable @SerialName("describe") data object Describe:StickerCommand
    @Serializable @SerialName("query") data class Query(val scope:StickerScope=StickerScope.ALL,val libraryId:String?=null,
        val offset:Int=0,val limit:Int=50,val revision:Long?=null):StickerCommand
    @Serializable @SerialName("query-frames") data class QueryFrames(val offset:Int=0,val limit:Int=50,val revision:Long?=null):StickerCommand
    @Serializable @SerialName("query-albums") data class QueryAlbums(val offset:Int=0,val limit:Int=50,val revision:Long?=null):StickerCommand
    @Serializable @SerialName("query-album-cards") data class QueryAlbumCards(val albumId:String,val offset:Int=0,val limit:Int=50,val revision:Long?=null):StickerCommand
    /** Tokens are caller-bound. Removal requires a private, native review confirmation. */
    @Serializable @SerialName("prepare") data class Prepare(val transactionId:String,val revision:Long,val edits:List<StickerEdit>):StickerCommand
    @Serializable @SerialName("commit") data class Commit(val token:String):StickerCommand
    @Serializable @SerialName("discard") data class Discard(val token:String):StickerCommand
    @Serializable @SerialName("transaction") data class Transaction(val transactionId:String):StickerCommand
    @Serializable @SerialName("publish-image") data class PublishImage(val source:PluginResource,val name:String="图片"):StickerCommand
    @Serializable @SerialName("create-qr-code") data class CreateQrCode(val text:String):StickerCommand
    /** Copies an existing quick-text image or motion-photo's still image into the gallery resource store. */
    @Serializable @SerialName("import-media") data class ImportMedia(val mediaId:String):StickerCommand
    /** Returns a durable copy beneath the caller's own storage, never another owner's path. */
    @Serializable @SerialName("read-image") data class ReadImage(val imageId:String,val destination:String):StickerCommand
    /** Publishes a host-owned image attachment. Requires quick-text-write as well as sticker access. */
    @Serializable @SerialName("publish-quick-media") data class PublishQuickMedia(val imageId:String):StickerCommand
    @Serializable @SerialName("export-card") data class ExportCard(val cardId:String,val destination:String):StickerCommand
    @Serializable @SerialName("open-ui") data class OpenUi(val target:StickerUiTarget,val id:String?=null):StickerCommand
}

@Serializable sealed interface StickerReply {
    @Serializable @SerialName("features") data class Features(val protocol:Int=1,val writable:Boolean,val operations:List<String>,val maxBatch:Int=100,val maxQuery:Int=50):StickerReply
    @Serializable @SerialName("snapshot") data class Snapshot(val value:StickerSnapshot):StickerReply
    @Serializable @SerialName("frames") data class Frames(val revision:Long,val items:List<PolaroidFrameStyle>,val total:Int,val nextOffset:Int?=null):StickerReply
    @Serializable @SerialName("albums") data class Albums(val revision:Long,val items:List<PolaroidAlbum>,val total:Int,val nextOffset:Int?=null):StickerReply
    @Serializable @SerialName("prepared") data class Prepared(val value:StickerPreview):StickerReply
    @Serializable @SerialName("applied") data class Applied(val revision:Long,val transactionId:String):StickerReply
    @Serializable @SerialName("discarded") data class Discarded(val token:String):StickerReply
    @Serializable @SerialName("image") data class Image(val value:StickerImage):StickerReply
    @Serializable @SerialName("resource") data class Resource(val source:PluginResource,val bytes:Long):StickerReply
    @Serializable @SerialName("media") data class Media(val value:QuickMedia):StickerReply
    @Serializable @SerialName("ui-opened") data class UiOpened(val target:StickerUiTarget):StickerReply
    @Serializable @SerialName("failure") data class Failure(val code:StickerError,val message:String,val revision:Long?=null,val reviewToken:String?=null):StickerReply
}
