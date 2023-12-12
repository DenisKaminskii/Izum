package com.polleo.data

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.polleo.api.custom.CustomPackJson
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
        val link: String,
        override val isUpdated: Boolean = false
    ) : Pack(id, title, isUpdated) {

        companion object {

            fun fromJson(json: CustomPackJson): Custom {
                return Custom(
                    id = json.id,
                    title = json.title,
                    description = json.description,
                    pollsCount = json.pollsCount,
                    token = json.token,
                    link = json.code
                )
            }

        }

    }

}

@Parcelize
data class PackPreview(
    val option1: String,
    val option2: String
) : Parcelable