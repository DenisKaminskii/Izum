package com.polleo.api.custom

import android.content.Context
import com.polleo.R
import com.polleo.data.Pack
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomPackJson(
    @Json(name = "id")
    val id: Long,
    @Json(name = "title")
    val title: String,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "pollsCount")
    val pollsCount: Long,
    @Json(name = "token")
    val token: String,
    @Json(name = "code")
    val code: String
)

fun CustomPackJson.toModel(
    isMine: Boolean,
    context: Context
) = Pack.Custom(
    id = id,
    title = title,
    description = description,
    pollsCount = pollsCount,
    token = token,
    code = code,
    isMine = isMine,
    contentColor = context.getColor(R.color.black_soft),
    gradientStartColor = context.getColor(R.color.white_gradient_start),
    gradientEndColor = context.getColor(R.color.white_gradient_end)
)