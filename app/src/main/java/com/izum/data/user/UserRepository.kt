package com.izum.data.user

import com.izum.data.DeviceIdProvider
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import com.izum.api.AuthApi
import com.izum.api.GetTokenRequest
import java.lang.Exception
import javax.inject.Inject

interface UserRepository  {

    @Throws(Exception::class)
    suspend fun fetchToken()

}

class UserRepositoryImpl @Inject constructor(
    private val preferenceCache: PreferenceCache,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider
) : UserRepository {

    override suspend fun fetchToken() {
        val response = authApi.getToken(
            GetTokenRequest(
                deviceId = deviceIdProvider.deviceId
            )
        )

        val token = response.token
        preferenceCache.putString(PreferenceKey.Token, token)
    }


}