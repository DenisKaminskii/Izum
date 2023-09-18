package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IdJson(
    @Json(name = "id")
    val id: String
)