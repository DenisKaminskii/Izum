package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path

interface PacksApi {

    @GET("packs")
    suspend fun getPacks() : List<PackJson>

}

@JsonClass(generateAdapter = true)
data class PackJson(
    @Json(name="id")
    val id: Long,
    @Json(name="title")
    val title: String,
    @Json(name="description")
    val description: String? = null,
    @Json(name="isPaid")
    val isPaid: Boolean,
    @Json(name="productId")
    val productId: String?,
    @Json(name="pollsCount")
    val pollsCount: Long,
    @Json(name="author")
    val author: AuthorJson? = null
)

@JsonClass(generateAdapter = true)
data class AuthorJson(
    @Json(name="id")
    val id: Long
)

@JsonClass(generateAdapter = true)
data class VoteJson(
    @Json(name="date")
    val date: String,
    @Json(name="optionId")
    val optionId: Long
)
