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
    ): List<PollJson>

    @POST("polls/{pollId}/vote")
    suspend fun vote(
        @Path("pollId") pollId: Long,
        @Body request: VoteRequestJson
    )

    @GET("polls/{pollId}/stats")
    suspend fun getPollStatistic(
        @Path("pollId") pollId: Long
    ): PollStatisticJson

    @POST("polls/suggest")
    suspend fun suggestPoll(
        @Body request: SuggestPollRequestJson
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

@JsonClass(generateAdapter = true)
data class PollStatisticOptionJson(
    @Json(name="id")
    val id: Long,
    @Json(name="votesCount")
    val votesCount: Long
)

@JsonClass(generateAdapter = true)
data class PollStatisticJson(
    @Json(name = "options")
    val options: List<PollOptionJson>,
    @Json(name = "sections")
    val sections: List<PollStatisticSectionJson>
)

@JsonClass(generateAdapter = true)
data class PollStatisticSectionJson(
    @Json(name = "title")
    val title: String,
    @Json(name = "categories")
    val categories: List<PollStatisticCategoryJson>
)

@JsonClass(generateAdapter = true)
data class PollStatisticCategoryJson(
    @Json(name = "title")
    val title: String,
    @Json(name = "options")
    val options: List<PollStatisticOptionJson>
)

@JsonClass(generateAdapter = true)
data class SuggestPollRequestJson(
    @Json(name = "options")
    val options: List<String>
)