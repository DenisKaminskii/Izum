package com.izum.api.statistic

import com.izum.api.PollOptionJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollStatisticJson(
    @Json(name = "options")
    val options: List<PollOptionJson>,
    @Json(name = "sections")
    val sections: List<PollStatisticSectionJson>
)