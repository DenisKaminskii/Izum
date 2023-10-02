package com.polleo.api.custom

import com.polleo.api.PollJson
import com.polleo.api.TitleJson
import com.polleo.api.statistic.PollStatisticJson
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CustomPackApi {

    @GET("custom-packs")
    suspend fun getPacks() : List<CustomPackJson>

    @DELETE("custom-packs/{id}")
    suspend fun deletePack(@Path("id") id: Long)

    @POST("custom-packs")
    suspend fun createPack(@Body request: TitleJson) : CustomPackJson

    @PATCH("custom-packs/{id}")
    suspend fun updatePack(@Path("id") id: Long, @Body request: TitleJson) : CustomPackJson

    @GET("custom-packs/{id}/polls")
    suspend fun getCustomPackPolls(@Path("id") id: Long) : List<PollJson>

    @POST("custom-packs/{id}/polls")
    suspend fun addPoll(@Path("id") id: Long, @Body request: CustomPackAddPollRequestJson) : PollJson

    @DELETE("custom-packs/polls/{pollId}")
    suspend fun removePoll(@Path("pollId") pollId: Long)

    @GET("custom-packs/polls/{pollId}/stats")
    suspend fun getPollStatistic(
        @Path("pollId") pollId: Long
    ): PollStatisticJson

}