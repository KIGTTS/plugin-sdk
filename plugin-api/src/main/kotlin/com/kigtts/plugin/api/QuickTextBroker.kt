package com.kigtts.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

/** Portable DTOs only. A broker is bound to the calling plugin, never to a caller-supplied owner. */
interface QuickTextBroker {
    suspend fun request(command: QuickTextCommand): QuickTextReply
    /** Invalidations, including the current state on subscription. Re-query after reconnect. */
    fun changes(afterRevision: Long = -1): Flow<QuickTextChange>
}

@Serializable enum class QuickCardKind { TEXT, AUDIO, MEDIA, PAGE }
@Serializable enum class QuickTextSelectionMode { SINGLE, MULTIPLE }
@Serializable enum class QuickColorKind { DEFAULT, CUSTOM, NONE }
@Serializable enum class QuickAudioPolicy { SILENT, AUTOMATIC, FOLLOW_READING }
@Serializable enum class QuickFormatKind { BOLD, ITALIC, UNDERLINE, COLOR, SIZE }
@Serializable data class QuickFormat(val start:Int,val end:Int,val kind:QuickFormatKind,val value:Long=0)
@Serializable data class QuickGroup(val id:String=UUID.randomUUID().toString(),val name:String,val icon:String="LIST")
@Serializable data class QuickEntry(val id:String=UUID.randomUUID().toString(),val groupId:String,val text:String,val title:String="",
    val kind:QuickCardKind=QuickCardKind.TEXT,val mediaId:String?=null,val colorKind:QuickColorKind=QuickColorKind.DEFAULT,
    val color:Long=0xFF00696E,val richText:Boolean=false,val formats:List<QuickFormat> = emptyList(),
    val autoPlay:Boolean=true,val audioPolicy:QuickAudioPolicy?=null,val metadata:Map<String,String> = emptyMap())
@Serializable data class QuickMedia(val id:String,val kind:String,val owner:String?=null,val version:Long,
    val mainPage:String?=null,val scale:String="FIT",val network:Boolean=false,val mime:String="",val url:String?=null,
    val thumbnailMode:String="AUTO",val focusX:Double=.5,val focusY:Double=.5,val motion:Boolean=false,val appleLive:Boolean=false)
@Serializable data class QuickTextSnapshot(val revision:Long,val groups:List<QuickGroup>,val entries:List<QuickEntry>,
    val offset:Int=0,val total:Int=entries.size,val nextOffset:Int?=null,val media:List<QuickMedia> = emptyList(),
    val selectedGroupId:String?=null,val editingEntryId:String?=null)
@Serializable data class QuickTextChange(val revision:Long,val pagesVersion:String="",val refresh:Boolean=true,val decisionsVersion:Long=0)
@Serializable data class QuickFileInfo(val path:String,val directory:Boolean,val bytes:Long,val main:Boolean=false)
@Serializable data class QuickBatchPreview(val token:String,val revision:Long,val added:Int,val updated:Int,val deleted:Int,
    val groupChanges:Int,val titles:List<String>,val requiresConfirmation:Boolean)
@Serializable data class QuickTextFeatures(val protocol:Int=1,val operations:List<String>,val writable:Boolean,
    val maxBatchOperations:Int=100,val maxQueryItems:Int=50,val groupIcons:List<String> = emptyList(),
    val features:Map<String,String> = mapOf("html-tools" to "subtitle-page","inline-widgets" to "false","archive-schema" to "1","notifications" to "invalidation","quick-text-selector" to "single,multiple"))
@Serializable sealed interface QuickFileEdit {
    @Serializable @SerialName("directory") data class Directory(val path:String):QuickFileEdit
    @Serializable @SerialName("put") data class Put(val path:String,val source:PluginResource):QuickFileEdit
    @Serializable @SerialName("move") data class Move(val path:String,val destination:String):QuickFileEdit
    @Serializable @SerialName("remove") data class Remove(val paths:List<String>):QuickFileEdit
    @Serializable @SerialName("main") data class Main(val path:String):QuickFileEdit
}

@Serializable sealed interface QuickTextEdit {
    @Serializable @SerialName("group.put") data class PutGroup(val group:QuickGroup):QuickTextEdit
    @Serializable @SerialName("entry.put") data class PutEntry(val entry:QuickEntry):QuickTextEdit
    @Serializable @SerialName("entry.remove") data class RemoveEntries(val ids:List<String>):QuickTextEdit
    @Serializable @SerialName("group.remove") data class RemoveGroups(val ids:List<String>,val moveTo:String?=null,val deleteEntries:Boolean=false):QuickTextEdit
    @Serializable @SerialName("entry.move") data class MoveEntries(val ids:List<String>,val groupId:String,val beforeId:String?=null):QuickTextEdit
    @Serializable @SerialName("entry.order") data class OrderEntries(val groupId:String,val ids:List<String>):QuickTextEdit
    @Serializable @SerialName("group.order") data class OrderGroups(val ids:List<String>):QuickTextEdit
}

@Serializable sealed interface QuickTextCommand {
    @Serializable @SerialName("describe") data object Describe:QuickTextCommand
    @Serializable @SerialName("query") data class Query(val groupId:String?=null,val offset:Int=0,val limit:Int=50,val revision:Long?=null,val includeContent:Boolean=true):QuickTextCommand
    /** Staged, all-or-nothing change; tokens are owner-bound and expire after ten minutes. */
    @Serializable @SerialName("prepare") data class Prepare(val transactionId:String,val revision:Long,val edits:List<QuickTextEdit>):QuickTextCommand
    @Serializable @SerialName("commit") data class Commit(val token:String):QuickTextCommand
    @Serializable @SerialName("discard") data class Discard(val token:String):QuickTextCommand
    @Serializable @SerialName("transaction") data class Transaction(val transactionId:String):QuickTextCommand
    @Serializable @SerialName("publish-page") data class PublishPage(val root:PluginResource,val mainPage:String="index.html",val mediaId:String?=null,
        val name:String="页面",val allowedActions:List<String> = emptyList(),val network:Boolean=false,val version:Long?=null):QuickTextCommand
    @Serializable @SerialName("publish-media") data class PublishMedia(val content:SubtitleContent):QuickTextCommand
    @Serializable @SerialName("publish-audio") data class PublishAudio(val source:PluginResource,val name:String="音频"):QuickTextCommand
    @Serializable @SerialName("files") data class Files(val mediaId:String,val directory:String="",val offset:Int=0,val limit:Int=100):QuickTextCommand
    @Serializable @SerialName("get-file") data class GetFile(val mediaId:String,val path:String,val destination:String):QuickTextCommand
    @Serializable @SerialName("put-file") data class PutFile(val mediaId:String,val path:String,val source:PluginResource,val version:Long):QuickTextCommand
    @Serializable @SerialName("set-main") data class SetMain(val mediaId:String,val path:String,val version:Long):QuickTextCommand
    @Serializable @SerialName("presentation") data class Presentation(val mediaId:String,val version:Long,val scale:String?=null,
        val thumbnailMode:String?=null,val focusX:Double?=null,val focusY:Double?=null):QuickTextCommand
    @Serializable @SerialName("edit-files") data class EditFiles(val mediaId:String,val version:Long,val edits:List<QuickFileEdit>):QuickTextCommand
    @Serializable @SerialName("export") data class Export(val groupIds:List<String> = emptyList(),val entryIds:List<String> = emptyList(),val includeMedia:Boolean=true,
        val destination:String="exports/quick-text.kigcards"):QuickTextCommand
    /** Import stages resources in the caller's Appdata, previews the library change, then uses Commit. */
    @Serializable @SerialName("import") data class Import(val source:PluginResource,val transactionId:String,val revision:Long):QuickTextCommand
    @Serializable @SerialName("open-ui") data class OpenUi(val target:QuickUiTarget,val id:String?=null):QuickTextCommand
}
@Serializable enum class QuickUiTarget { MANAGE, NEW_ENTRY, EDIT_ENTRY, PAGE_FILES, PAGE_PREVIEW, REVIEW }

@Serializable enum class QuickTextError { PERMISSION_DENIED, NOT_FOUND, INVALID, CONFLICT, BUSY, CONFIRMATION_REQUIRED, HOST_UNAVAILABLE, TOO_LARGE }
@Serializable sealed interface QuickTextReply {
    @Serializable @SerialName("features") data class Features(val value:QuickTextFeatures):QuickTextReply
    @Serializable @SerialName("snapshot") data class Snapshot(val value:QuickTextSnapshot):QuickTextReply
    @Serializable @SerialName("prepared") data class Prepared(val value:QuickBatchPreview,val mapping:PluginResource?=null):QuickTextReply
    @Serializable @SerialName("applied") data class Applied(val revision:Long,val transactionId:String,val mapping:PluginResource?=null):QuickTextReply
    @Serializable @SerialName("discarded") data class Discarded(val token:String):QuickTextReply
    @Serializable @SerialName("media") data class Media(val value:QuickMedia):QuickTextReply
    @Serializable @SerialName("files") data class Files(val version:Long,val items:List<QuickFileInfo>,val total:Int=items.size,val nextOffset:Int?=null):QuickTextReply
    @Serializable @SerialName("resource") data class Resource(val source:PluginResource,val bytes:Long):QuickTextReply
    @Serializable @SerialName("ui-opened") data class UiOpened(val target:QuickUiTarget):QuickTextReply
    @Serializable @SerialName("failure") data class Failure(val code:QuickTextError,val message:String,val revision:Long?=null,val reviewToken:String?=null):QuickTextReply
}
