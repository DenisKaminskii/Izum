package com.izum.di

import android.content.Context
import com.izum.data.DeviceIdProvider
import com.izum.data.DeviceIdProviderImpl
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceCacheImpl
import com.izum.ui.route.Router
import com.izum.ui.route.RouterImpl
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
        @ApplicationContext context: Context
    ) : PreferenceCache {
        return PreferenceCacheImpl(context)
    }

    @Provides
    fun provideRouter() : Router {
        return RouterImpl()
    }

}