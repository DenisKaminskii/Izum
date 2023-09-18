package com.izum.api.custom

import com.izum.api.PollJson
import com.izum.api.TitleJson
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
    suspend fun addPoll(@Path("id") id: Long, @Body request: CustomPackAddPollRequestJson) : List<PollJson>

}