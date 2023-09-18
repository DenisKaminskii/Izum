package com.izum.api.custom

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
    @Json(name = "link")
    val link: String
)