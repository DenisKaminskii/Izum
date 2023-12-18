package com.pickone.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollOptionJson(
    @Json(name="id")
    val id: Long,
    @Json(name="title")
    val title: String,
    @Json(name="votesCount")
    val votesCount: Long,
    @Json(name="createdAt")
    val createdAt: String? = null
)