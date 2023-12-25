package com.pickone.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VoteRequestJson(
    @Json(name = "optionId")
    val optionId: Long,
    @Json(name = "elapsedTime")
    val elapsedTimeMs: Int
)