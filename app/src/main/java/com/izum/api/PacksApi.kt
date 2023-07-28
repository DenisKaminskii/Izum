package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface PacksApi {

    @GET("packs")
    suspend fun getPacks() : List<PackJson>

    @GET("packs/{packId}/polls")
    suspend fun getPackPolls(
        @Path("packId") packId: Long
    ) : List<PollJson>

}

@JsonClass(generateAdapter = true)
data class PackJson(
    @Json(name="id")
    val id: Long,
    @Json(name="title")
    val title: String,
    @Json(name="description")
    val description: String,
    @Json(name="isPaid")
    val isPaid: Boolean,
    @Json(name="productId")
    val productId: String?,
    @Json(name="pollsCount")
    val pollsCount: Long,
    @Json(name="author")
    val author: AuthorJson
)

@JsonClass(generateAdapter = true)
data class AuthorJson(
    @Json(name="id")
    val id: Long
)

@JsonClass(generateAdapter = true)
data class PollJson(
    @Json(name="id")
    val id: Long
)