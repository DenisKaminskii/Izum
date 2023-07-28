package com.izum.di

import com.izum.data.DeviceIdProvider
import com.izum.domain.core.PreferenceCache
import com.izum.api.AuthApi
import com.izum.data.user.UserRepository
import com.izum.data.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        preferenceCache: PreferenceCache,
        authApi: AuthApi,
        deviceIdProvider: DeviceIdProvider
    ) : UserRepository {
        return UserRepositoryImpl(
            preferenceCache,
            authApi,
            deviceIdProvider
        )
    }

}