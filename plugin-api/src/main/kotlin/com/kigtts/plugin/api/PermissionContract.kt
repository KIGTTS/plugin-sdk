package com.kigtts.plugin.api

/** Declare in the package manifest and request immediately before using an optional capability. */
suspend fun PluginEnvironment.requirePermission(permission: String) {
    val reply = appBroker?.request(AppCommand.Permission(permission))
    check(reply is AppReply.Data && reply.value["allowed"]?.toString() == "true") { "PERMISSION_REQUIRED: $permission" }
}

fun quickTextPermission(command: QuickTextCommand, capabilities: Set<String>): String =
    if (command is QuickTextCommand.Query || command is QuickTextCommand.Describe || command is QuickTextCommand.Transaction ||
        command is QuickTextCommand.Files || command is QuickTextCommand.GetFile || command is QuickTextCommand.Export)
        if ("quick-text-read" in capabilities) "quick-text-read" else "quick-text-write" else "quick-text-write"

fun stickerPermission(command: StickerCommand, capabilities: Set<String>): String =
    if (command is StickerCommand.Describe || command is StickerCommand.Query || command is StickerCommand.Transaction ||
        command is StickerCommand.QueryFrames || command is StickerCommand.QueryAlbums || command is StickerCommand.QueryAlbumCards ||
        command is StickerCommand.ReadImage || command is StickerCommand.ExportCard || command is StickerCommand.PublishQuickMedia)
        if ("stickers-read" in capabilities) "stickers-read" else "stickers-write" else "stickers-write"
