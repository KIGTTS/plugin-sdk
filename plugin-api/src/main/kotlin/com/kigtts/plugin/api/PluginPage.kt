package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Optional presentation metadata. Page reads remain pure; entry work is an explicit host action. */
@Serializable
data class PluginPage(val id: String, val title: String, val blocks: List<PageBlock>,
    val management: ManagementList? = null, val header: ManagementItem? = null, val onEnterAction: String? = null,
    val loading: Boolean = false, val refreshAction: String? = null) {
    // Keep the API9–14 JVM constructor/default masks and copy method for installed packages.
    constructor(id: String, title: String, blocks: List<PageBlock>, management: ManagementList? = null)
        : this(id, title, blocks, management, null, null)
    // Preserve the API15 constructor and default-mask entry point, too.
    constructor(id: String, title: String, blocks: List<PageBlock>, management: ManagementList? = null,
        header: ManagementItem? = null, onEnterAction: String? = null)
        : this(id, title, blocks, management, header, onEnterAction, false, null)
    fun copy(id: String = this.id, title: String = this.title, blocks: List<PageBlock> = this.blocks,
        management: ManagementList? = this.management): PluginPage =
        PluginPage(id, title, blocks, management, header, onEnterAction, loading, refreshAction)
    fun copy(id: String = this.id, title: String = this.title, blocks: List<PageBlock> = this.blocks,
        management: ManagementList? = this.management, header: ManagementItem? = this.header,
        onEnterAction: String? = this.onEnterAction): PluginPage =
        PluginPage(id, title, blocks, management, header, onEnterAction, loading, refreshAction)
}
