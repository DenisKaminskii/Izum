package com.polleo.di

import android.content.Context
import com.polleo.R
import com.polleo.api.custom.CustomPackApi
import com.polleo.domain.core.PreferenceCache
import com.polleo.network.HeadersInterceptor
import com.polleo.api.UserApi
import com.polleo.api.PackApi
import com.polleo.api.PollApi
import com.polleo.api.TokenApi
import com.polleo.network.TokenInterceptor
import com.polleo.data.DeviceIdProvider
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
import javax.inject.Named
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
    ): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    fun providePacksApi(
        retrofit: Retrofit
    ): PackApi {
        return retrofit.create(PackApi::class.java)
    }

    @Provides
    fun provideCustomPacksApi(
        retrofit: Retrofit
    ): CustomPackApi {
        return retrofit.create(CustomPackApi::class.java)
    }

    @Provides
    fun providePollsApi(
        retrofit: Retrofit
    ): PollApi {
        return retrofit.create(PollApi::class.java)
    }

    // Exception cause of cycle dependency
    @Provides
    fun provideTokenApi(
        @ApplicationContext context: Context,
        moshi: Moshi,
        @Named("token") okHttpClient: OkHttpClient
    ): TokenApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.backend_url))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

        return retrofit.create(TokenApi::class.java)
    }

    @Provides
    fun provideRetrofit(
        moshi: Moshi,
        @Named("base") okHttpClient: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl("https://octopus-app-ztlqh.ondigitalocean.app/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Named("base")
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        tokenApi: TokenApi,
        deviceIdProvider: DeviceIdProvider,
        preferenceCache: PreferenceCache
    ): OkHttpClient {
        val cacheFolder = File(context.cacheDir, CACHE_FOLDER)
        val cache = Cache(cacheFolder, CACHE_SIZE)

        return OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(tokenApi, preferenceCache, deviceIdProvider))
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

    @Provides
    @Named("token")
    @Singleton
    fun provideTokenOkHttpClient(@ApplicationContext context: Context) : OkHttpClient {
        val cacheFolder = File(context.cacheDir, CACHE_FOLDER)
        val cache = Cache(cacheFolder, CACHE_SIZE)

        return OkHttpClient.Builder()
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