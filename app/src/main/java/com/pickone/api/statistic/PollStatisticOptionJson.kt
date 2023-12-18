package com.pickone.api.statistic

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollStatisticOptionJson(
    @Json(name="id")
    val id: Long,
    @Json(name="votesCount")
    val votesCount: Long
)