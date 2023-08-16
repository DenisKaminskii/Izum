package com.izum.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {

    @PATCH("user/info")
    suspend fun updateUserInfo(@Body request: UpdateUserInfoRequest)

}

@JsonClass(generateAdapter = true)
data class UpdateUserInfoRequest(
    @Json(name = "age")
    val age: Int,
    @Json(name = "gender")
    val gender: String
)