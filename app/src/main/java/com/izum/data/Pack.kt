package com.izum.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pack(
    val id: Long,
    val title: String,
    val description: String?,
    val isPaid: Boolean,
    val productId: String?,
    val pollsCount: Long,
    val authorId: Long?,
    @ColorInt val contentColor: Int,
    @ColorInt val gradientStartColor: Int,
    @ColorInt val gradientEndColor: Int,
    val preview: List<PackPreview>
) : Parcelable

@Parcelize
data class PackPreview(
    val option1: String,
    val option2: String
) : Parcelable