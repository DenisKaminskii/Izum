package com.izum.data.repository

import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import com.izum.api.AuthApi
import com.izum.api.GetTokenRequest
import com.izum.data.DeviceIdProvider
import com.izum.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UserRepository  {

    var hasSubscription: Boolean

    suspend fun fetchToken()

}

class UserRepositoryImpl(
    private val preferenceCache: PreferenceCache,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override var hasSubscription: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.HasSubscription, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.HasSubscription, value)
        }

    override suspend fun fetchToken() = withContext(ioDispatcher) {
        val response = authApi.getToken(
            GetTokenRequest(
                deviceId = deviceIdProvider.deviceId
            )
        )

        val token = response.token
        preferenceCache.putString(PreferenceKey.Token, token)
    }


}