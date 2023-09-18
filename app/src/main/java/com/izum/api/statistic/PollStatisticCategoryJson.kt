package com.izum.api.statistic

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollStatisticCategoryJson(
    @Json(name = "title")
    val title: String,
    @Json(name = "options")
    val options: List<PollStatisticOptionJson>
)