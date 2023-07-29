package com.izum.di

import com.izum.data.DeviceIdProvider
import com.izum.domain.core.PreferenceCache
import com.izum.api.AuthApi
import com.izum.data.repository.UserRepository
import com.izum.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        preferenceCache: PreferenceCache,
        authApi: AuthApi,
        deviceIdProvider: DeviceIdProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : UserRepository {
        return UserRepositoryImpl(
            preferenceCache,
            authApi,
            deviceIdProvider,
            ioDispatcher
        )
    }

}