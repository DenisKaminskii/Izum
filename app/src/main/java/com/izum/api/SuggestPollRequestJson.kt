package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SuggestPollRequestJson(
    @Json(name = "options")
    val options: List<String>
)