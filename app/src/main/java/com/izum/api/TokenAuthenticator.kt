package com.izum.api

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.data.DeviceIdProvider
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import javax.net.ssl.HttpsURLConnection

class TokenAuthenticator(
    private val tokenApi: TokenApi,
    private val preferenceCache: PreferenceCache,
    private val deviceIdProvider: DeviceIdProvider
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val usedToken = response.request.header(HeadersInterceptor.KEY_HEADER_AUTHORIZATION) ?: return null
        val newToken = runBlocking { getOrUpdateToken(usedToken) } ?: return null

        return response.request.newBuilder()
            .header(HeadersInterceptor.KEY_HEADER_AUTHORIZATION, newToken)
            .build()
    }

    @WorkerThread
    private suspend fun getOrUpdateToken(oldToken: String): String? = mutex.withLock {
        val currentToken = preferenceCache.getString(PreferenceKey.Token)

        if (currentToken != null && currentToken != oldToken) {
            return currentToken
        }

        try {
            val response = tokenApi.getToken(GetTokenRequest(deviceId = deviceIdProvider.deviceId))
            val token = response.token
            preferenceCache.putString(PreferenceKey.Token, token)

            return token
        }catch (e: Exception) {
            val unauthorized = HttpsURLConnection.HTTP_UNAUTHORIZED
            val forbidden = HttpsURLConnection.HTTP_FORBIDDEN
            if (e is HttpException && (e.code() == unauthorized || e.code() == forbidden)) {
                preferenceCache.putString(PreferenceKey.Token, null)
            }
            Log.d("Steve", "Error while fetching token", e)

            return null
        }
    }

}