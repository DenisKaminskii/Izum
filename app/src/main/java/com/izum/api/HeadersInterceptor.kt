package com.izum.api

import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(
    private val preferenceCache: PreferenceCache
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val token = preferenceCache.getString(PreferenceKey.Token)
        request.addHeader("Authorization", token)

        return chain.proceed(request.build())
    }

}