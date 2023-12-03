package com.polleo.network

import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceKey
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(
    private val preferenceCache: PreferenceCache
) : Interceptor {

    companion object {
        const val KEY_HEADER_AUTHORIZATION = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val token = preferenceCache.getString(PreferenceKey.Token)
        request.header(KEY_HEADER_AUTHORIZATION, token ?: "")

        return chain.proceed(request.build())
    }

}