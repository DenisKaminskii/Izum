package com.pickone.data

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.pickone.api.PackJson
import kotlinx.parcelize.Parcelize

sealed class Pack(
    open val id: Long,
    open val title: String,
    open val description: String?,
    open val isPaid: Boolean,
    open val pollsCount: Long,
    @ColorInt open val contentColor: Int,
    @ColorInt open val gradientStartColor: Int,
    @ColorInt open val gradientEndColor: Int
) : Parcelable {

    @Parcelize
    data class Public(
        override val id: Long,
        override val title: String,
        override val description: String?,
        override val isPaid: Boolean,
        val productId: String?,
        override val pollsCount: Long,
        val authorId: Long?,
        @ColorInt override val contentColor: Int,
        @ColorInt override val gradientStartColor: Int,
        @ColorInt override val gradientEndColor: Int,
        val preview: List<PackPreview>
    ) : Pack(
        id,
        title,
        description,
        isPaid,
        pollsCount,
        contentColor,
        gradientStartColor,
        gradientEndColor
    ) {

        companion object {

            fun fromJson(packJson: PackJson) : Public = Public(
                id = packJson.id,
                title = packJson.title,
                description = packJson.description,
                isPaid = packJson.isPaid,
                productId = packJson.productId,
                pollsCount = packJson.pollsCount,
                authorId = packJson.author?.id,
                gradientStartColor = Color.parseColor(packJson.gradientStartColor),
                gradientEndColor = Color.parseColor(packJson.gradientEndColor),
                contentColor = Color.parseColor(packJson.contentColor),
                preview = packJson.preview.map { preview ->
                    PackPreview(
                        option1 = preview.option1,
                        option2 = preview.option2
                    )
                }
            )
        }

    }

    @Parcelize
    data class Custom(
        override val id: Long,
        override val title: String,
        override val description: String?,
        override val pollsCount: Long,
        @ColorInt override val contentColor: Int,
        @ColorInt override val gradientStartColor: Int,
        @ColorInt override val gradientEndColor: Int,
        val token: String,
        val code: String,
        val isMine: Boolean
    ) : Pack(
        id,
        title,
        description,
        false,
        pollsCount,
        contentColor,
        gradientStartColor,
        gradientEndColor
    )

}

@Parcelize
data class PackPreview(
    val option1: String,
    val option2: String
) : Parcelable


fun String.retrieveCodeData() : Pair<Long, String>? {
    val split = split("-")
    if (split.size < 2) return null

    val id = split[0].toLongOrNull() ?: return null
    val name = split[1].takeIf { it.isNotBlank() } ?: return null

    return id to name
}