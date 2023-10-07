package com.polleo.di

import android.content.Context
import com.polleo.data.DeviceIdProvider
import com.polleo.data.DeviceIdProviderImpl
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceCacheImpl
import com.polleo.ui.route.Router
import com.polleo.ui.route.RouterImpl
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
    fun provideRouter(
        preferenceCache: PreferenceCache
    ) : Router {
        return RouterImpl(preferenceCache)
    }

}