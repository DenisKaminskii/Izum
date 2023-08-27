package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CustomPacksApi {

    @GET("custom-packs")
    suspend fun getPacks() : List<CustomPackJson>

    @DELETE("custom-packs/{id}")
    suspend fun deletePack(@Path("id") id: Long)

    @POST("custom-packs")
    suspend fun createPack(@Body request: CreatePackRequestJson) : CustomPackJson

}

@JsonClass(generateAdapter = true)
data class CreatePackRequestJson(
    @Json(name = "title")
    val title: String
)

@JsonClass(generateAdapter = true)
data class CustomPackJson(
    @Json(name = "id")
    val id: Long,
    @Json(name = "title")
    val title: String,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "pollsCount")
    val pollsCount: Long,
    @Json(name = "token")
    val token: String,
    @Json(name = "link")
    val link: String
)