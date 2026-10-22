package com.kigtts.plugin.api

const val DEFAULT_POLAROID_PAPER_COLOR: Long = 0xFFFFFFFFL
private const val LEGACY_CREAM_PAPER_COLOR: Long = 0xFFF7F2E8L
private val defaultPaperStyles = setOf("classic", "instax.mini.classic", "instax.square.classic", "instax.wide.classic")

/** Migrate only the host's former default paper; custom/brand styles retain their colors. */
fun PolaroidCard.normalizeLegacyPaper(): PolaroidCard =
    if (frameColor == LEGACY_CREAM_PAPER_COLOR && frameStyleId in defaultPaperStyles)
        copy(frameColor = DEFAULT_POLAROID_PAPER_COLOR) else this

/** Shared by native previews and PNG export, including uncommitted editor drafts. */
fun polaroidPaperColor(card: PolaroidCard, frame: PolaroidFrameStyle? = null): Long =
    frame?.color ?: if (frame?.owner != null || frame?.overlayImageId != null) card.frameColor
        else card.normalizeLegacyPaper().frameColor
