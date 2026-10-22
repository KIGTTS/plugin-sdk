package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

/** Portable .kigcards ZIP: UTF-8 cards.json plus relative media/ paths; never device paths. */
@Serializable data class QuickCardsBundle(val schema:Int=1,val groups:List<QuickGroup>,val entries:List<QuickEntry>,
    val media:List<QuickArchiveMedia> = emptyList())
@Serializable data class QuickArchiveMedia(val id:String,val kind:String,val primary:String,val mime:String="",val video:String?=null,
    val root:String?=null,val network:Boolean=false,val appleLive:Boolean=false,val scale:String="FIT",
    val thumbnailMode:String="AUTO",val focusX:Double=.5,val focusY:Double=.5)
