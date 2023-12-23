package com.pickone.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenApi {

    @POST("auth/device")
    suspend fun getToken(@Body request: GetTokenRequest) : GetTokenResponse

}

@JsonClass(generateAdapter = true)
data class GetTokenRequest(
    @Json(name = "deviceId")
    val deviceId: String
)

@JsonClass(generateAdapter = true)
data class GetTokenResponse(
    @Json(name = "token")
    val token: String,
    @Json(name = "userId")
    val userId: Long
)