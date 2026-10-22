package com.kigtts.plugin.api

import kotlinx.serialization.Serializable
import java.util.UUID

const val DEFAULT_POLAROID_ALBUM_ID = "user.polaroids"

/** A photo belongs to exactly one album; cardIds is that album's display order. */
@Serializable data class PolaroidAlbum(val id:String=UUID.randomUUID().toString(),val name:String,
    val cardIds:List<String> = emptyList())
