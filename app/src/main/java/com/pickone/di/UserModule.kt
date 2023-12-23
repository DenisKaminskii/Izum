package com.pickone.di

import com.pickone.api.TokenApi
import com.pickone.api.UserApi
import com.pickone.data.DeviceIdProvider
import com.pickone.data.repository.UserRepository
import com.pickone.data.repository.UserRepositoryImpl
import com.pickone.domain.core.PreferenceCache
import com.pickone.network.OnUserIdUpdated
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
        userApi: UserApi,
        tokenApi: TokenApi,
        deviceIdProvider: DeviceIdProvider,
        onUserIdUpdated: OnUserIdUpdated,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : UserRepository {
        return UserRepositoryImpl(
            preferenceCache,
            userApi,
            tokenApi,
            deviceIdProvider,
            onUserIdUpdated,
            ioDispatcher
        )
    }

}