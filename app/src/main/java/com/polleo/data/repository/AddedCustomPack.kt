package com.polleo.data.repository

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class AddedCustomPack(
    @Json(name = "id")
    val id: Long,
    @Json(name = "token")
    val token: String
) : Parcelable