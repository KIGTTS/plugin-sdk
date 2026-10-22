package com.kigtts.plugin.api

import kotlinx.serialization.Serializable

@Serializable enum class PolaroidFormat { LEGACY, MINI, SQUARE, WIDE;
    val geometry:PolaroidGeometry get()=when(this) {
        LEGACY->PolaroidGeometry(1200.0,1500.0,64.0,64.0,1072.0,1128.0,80.0,1260.0,1040.0,56.0,1340.0,32.0,false)
        MINI->modern(54.0,86.0,46.0,62.0)
        SQUARE->modern(72.0,86.0,62.0,62.0)
        WIDE->modern(108.0,86.0,99.0,62.0)
    }
    private fun modern(paperWidth:Double,paperHeight:Double,photoWidth:Double,photoHeight:Double)=
        PolaroidGeometry(paperWidth,paperHeight,(paperWidth-photoWidth)/2,6.0,photoWidth,photoHeight,
            (paperWidth-photoWidth)/2,71.6,photoWidth,3.2,77.0,2.0,true)
}

/** Physical paper/photo sizes are supplied values. Top=6 and caption positions are app layout choices. */
data class PolaroidGeometry(val paperWidth:Double,val paperHeight:Double,val photoLeft:Double,val photoTop:Double,
    val photoWidth:Double,val photoHeight:Double,val titleX:Double,val titleY:Double,val titleWidth:Double,val titleSize:Double,
    val descriptionY:Double,val descriptionSize:Double,val millimeterSizing:Boolean) {
    val aspectRatio:Double get()=paperWidth/paperHeight
    val photoAspectRatio:Double get()=photoWidth/photoHeight
    val photoRight:Double get()=photoLeft+photoWidth
    val photoBottom:Double get()=photoTop+photoHeight
}
