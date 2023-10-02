package com.polleo.network

import android.util.Log
import androidx.annotation.WorkerThread
import com.polleo.api.GetTokenRequest
import com.polleo.api.TokenApi
import com.polleo.data.DeviceIdProvider
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceKey
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

class TokenInterceptor(
    private val tokenApi: TokenApi,
    private val preferenceCache: PreferenceCache,
    private val deviceIdProvider: DeviceIdProvider
) : Interceptor {

    private val mutex = Mutex()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val currentToken = preferenceCache.getString(PreferenceKey.Token)

        val authenticatedRequest = request.newBuilder()
            .header(HeadersInterceptor.KEY_HEADER_AUTHORIZATION, currentToken ?: "")
            .build()

        val response = chain.proceed(authenticatedRequest)

        if (response.code == HttpsURLConnection.HTTP_UNAUTHORIZED || response.code == HttpsURLConnection.HTTP_FORBIDDEN) {
            val newToken = runBlocking { getOrUpdateToken(currentToken ?: "") }
            if (newToken != null) {
                val newRequest = request.newBuilder()
                    .header(HeadersInterceptor.KEY_HEADER_AUTHORIZATION, newToken)
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return response
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
        } catch (e: Exception) {
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
