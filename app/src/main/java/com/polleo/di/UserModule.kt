package com.polleo.di

import com.polleo.api.UserApi
import com.polleo.data.repository.UserRepository
import com.polleo.data.repository.UserRepositoryImpl
import com.polleo.domain.core.PreferenceCache
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
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : UserRepository {
        return UserRepositoryImpl(
            preferenceCache,
            userApi,
            ioDispatcher
        )
    }

}