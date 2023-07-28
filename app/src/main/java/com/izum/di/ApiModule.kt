package com.izum.di

import android.content.Context
import com.izum.domain.core.PreferenceCache
import com.izum.api.HeadersInterceptor
import com.izum.api.AuthApi
import com.izum.api.PacksApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    companion object {
        private const val CACHE_SIZE = (20 * 1024 * 1024).toLong()
        private const val CACHE_FOLDER = "okhttp"
    }

    @Provides
    fun provideUserApi(
        retrofit: Retrofit
    ) : AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    fun providePacksApi(
        retrofit: Retrofit
    ) : PacksApi {
        return retrofit.create(PacksApi::class.java)
    }

    @Provides
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl("https://octopus-app-ztlqh.ondigitalocean.app/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        moshi: Moshi,
        preferenceCache: PreferenceCache
    ) : OkHttpClient {
        val cacheFolder = File(context.cacheDir, CACHE_FOLDER)
        val cache = Cache(cacheFolder, CACHE_SIZE)

        return OkHttpClient.Builder()
            .addInterceptor(HeadersInterceptor(preferenceCache))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }


}