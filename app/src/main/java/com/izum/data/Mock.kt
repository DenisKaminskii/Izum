package com.izum.data

import com.izum.ui.pack.PackPreviewItem

object Mock {

    fun getPreviewItems(publicPack: Pack.Public) : List<PackPreviewItem> {
        return listOf(
            PackPreviewItem(
                topText = "Что-то типо того",
                bottomText = "Или что-то типо этого",
                textColor = publicPack.contentColor
            ),
            PackPreviewItem(
                topText = "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                bottomText = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout.",
                textColor = publicPack.contentColor
            ),
            PackPreviewItem(
                topText = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable.",
                bottomText = "Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable",
                textColor = publicPack.contentColor
            )
        )
    }

}