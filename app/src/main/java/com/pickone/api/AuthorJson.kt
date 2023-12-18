package com.pickone.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthorJson(
    @Json(name="id")
    val id: Long
)