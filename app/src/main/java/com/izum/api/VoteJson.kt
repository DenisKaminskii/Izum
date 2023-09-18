package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VoteJson(
    @Json(name="date")
    val date: String,
    @Json(name="optionId")
    val optionId: Long
)