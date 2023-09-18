package com.izum.api.custom

import com.izum.api.TitleJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomPackAddPollRequestJson(
    @Json(name = "options")
    val options: List<TitleJson>
)