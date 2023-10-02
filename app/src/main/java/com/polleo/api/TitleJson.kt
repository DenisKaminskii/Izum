package com.polleo.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TitleJson(
    @Json(name = "title")
    val title: String
)