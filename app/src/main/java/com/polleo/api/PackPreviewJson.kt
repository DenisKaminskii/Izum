package com.polleo.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PackPreviewJson(
    @Json(name="option1")
    val option1: String,
    @Json(name="option2")
    val option2: String
)