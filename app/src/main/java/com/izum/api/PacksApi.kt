package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

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
    val author: AuthorJson? = null,
    @Json(name="contentColor")
    val contentColor: String,
    @Json(name="gradientStartColor")
    val gradientStartColor: String,
    @Json(name="gradientEndColor")
    val gradientEndColor: String,
    @Json(name="preview")
    val preview: List<PackPreviewJson>
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

@JsonClass(generateAdapter = true)
data class PackPreviewJson(
    @Json(name="option1")
    val option1: String,
    @Json(name="option2")
    val option2: String
)
