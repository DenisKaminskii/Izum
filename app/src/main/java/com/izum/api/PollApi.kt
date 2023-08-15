package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PollApi {

    @GET("packs/{packId}/polls")
    suspend fun getPackPolls(
        @Path("packId") packId: Long
    ) : List<PollJson>

   @POST("polls/{pollId}/vote")
    suspend fun vote(
       @Path("pollId") pollId: Long,
       @Body request: VoteRequestJson
    )

}

@JsonClass(generateAdapter = true)
data class VoteRequestJson(
    @Json(name="optionId")
    val optionId: Long
)

@JsonClass(generateAdapter = true)
data class PollJson(
    @Json(name="id")
    val id: Long,
    @Json(name="packId")
    val packId: Long,
    @Json(name="options")
    val options: List<PollOptionJson>,
    @Json(name="voted")
    val vote: VoteJson? = null
)

@JsonClass(generateAdapter = true)
data class PollOptionJson(
    @Json(name="id")
    val id: Long,
    @Json(name="title")
    val title: String,
    @Json(name="votesCount")
    val votesCount: Long
)