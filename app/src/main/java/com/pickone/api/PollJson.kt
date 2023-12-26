package com.pickone.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PollJson(
    @Json(name = "id")
    val id: Long,
    @Json(name = "packId")
    val packId: Long,
    @Json(name = "author")
    val author: AuthorJson?,
    @Json(name = "options")
    val options: List<PollOptionJson>,
    @Json(name = "totalVotesCount")
    val totalVotesCount: Long?,
    @Json(name = "voted")
    val voted: VoteJson?,
    @Json(name = "createdAt")
    val createdAt: String?
)