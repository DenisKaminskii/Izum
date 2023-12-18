package com.pickone.di

import android.content.Context
import com.pickone.data.DeviceIdProvider
import com.pickone.data.DeviceIdProviderImpl
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceCacheImpl
import com.pickone.ui.route.Router
import com.pickone.ui.route.RouterImpl
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoreModule {

    @Provides
    @Singleton
    fun provideDeviceIdProvider(
        @ApplicationContext context: Context
    ) : DeviceIdProvider {
        return DeviceIdProviderImpl(context)
    }

    @Provides
    @Singleton
    fun providePreferenceCache(
        @ApplicationContext context: Context,
        moshi: Moshi
    ) : PreferenceCache {
        return PreferenceCacheImpl(context, moshi)
    }

    @Provides
    fun provideRouter(
        preferenceCache: PreferenceCache
    ) : Router {
        return RouterImpl(preferenceCache)
    }

}