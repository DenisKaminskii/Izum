package com.pickone.network

import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
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

        val token = preferenceCache.getString(PreferenceKey.Token.name)
        request.header(KEY_HEADER_AUTHORIZATION, token ?: "")

        return chain.proceed(request.build())
    }

}