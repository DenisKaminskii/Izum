package com.pickone.network

import timber.log.Timber
import androidx.annotation.WorkerThread
import com.pickone.api.GetTokenRequest
import com.pickone.api.TokenApi
import com.pickone.data.DeviceIdProvider
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
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
    private val deviceIdProvider: DeviceIdProvider,
    private val onUserIdUpdated: OnUserIdUpdated
) : Interceptor {

    private val mutex = Mutex()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val currentToken = preferenceCache.getString(PreferenceKey.Token.name)

        val authenticatedRequest = request.newBuilder()
            .header(HeadersInterceptor.KEY_HEADER_AUTHORIZATION, currentToken ?: "")
            .build()

        val response = chain.proceed(authenticatedRequest)

        if (response.code == HttpsURLConnection.HTTP_UNAUTHORIZED || response.code == HttpsURLConnection.HTTP_FORBIDDEN) {
            if (request.method == "GET" && request.url.encodedPath.contains("custom-packs/")) {
                return response
            }

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
        val currentToken = preferenceCache.getString(PreferenceKey.Token.name)

        if (currentToken != null && currentToken != oldToken) {
            return currentToken
        }

        try {
            val response = tokenApi.getToken(GetTokenRequest(deviceId = deviceIdProvider.deviceId))
            val token = response.token
            val userId = response.userId
            preferenceCache.putString(PreferenceKey.Token.name, token)
            preferenceCache.putLong(PreferenceKey.UserId.name, userId)

            onUserIdUpdated.emit(userId)

            return token
        } catch (e: Exception) {
            val unauthorized = HttpsURLConnection.HTTP_UNAUTHORIZED
            val forbidden = HttpsURLConnection.HTTP_FORBIDDEN
            if (e is HttpException && (e.code() == unauthorized || e.code() == forbidden)) {
                preferenceCache.putString(PreferenceKey.Token.name, null)
            }
            Timber.e(e, "Error while fetching token")

            return null
        }
    }
}
