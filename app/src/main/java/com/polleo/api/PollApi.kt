package com.polleo.api

import com.polleo.api.statistic.PollStatisticJson
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