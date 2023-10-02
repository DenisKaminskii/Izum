package com.polleo.api.statistic

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollStatisticSectionJson(
    @Json(name = "title")
    val title: String,
    @Json(name = "categories")
    val categories: List<PollStatisticCategoryJson>
)