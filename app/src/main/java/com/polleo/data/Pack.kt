package com.polleo.data

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.polleo.api.PackJson
import kotlinx.parcelize.Parcelize

sealed class Pack(
    open val id: Long,
    open val title: String,
    open val isUpdated: Boolean
) : Parcelable {

    @Parcelize
    data class Public(
        override val id: Long,
        override val title: String,
        val description: String?,
        val isPaid: Boolean,
        val productId: String?,
        val pollsCount: Long,
        val authorId: Long?,
        @ColorInt val contentColor: Int,
        @ColorInt val gradientStartColor: Int,
        @ColorInt val gradientEndColor: Int,
        val preview: List<PackPreview>,
        override val isUpdated: Boolean = false
    ) : Pack(id, title, isUpdated) {

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
        val description: String?,
        val pollsCount: Long,
        val token: String,
        val code: String,
        val isMine: Boolean,
        override val isUpdated: Boolean = false
    ) : Pack(id, title, isUpdated)

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