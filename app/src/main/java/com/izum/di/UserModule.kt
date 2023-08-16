package com.izum.di

import com.izum.api.UserApi
import com.izum.data.repository.UserRepository
import com.izum.data.repository.UserRepositoryImpl
import com.izum.domain.core.PreferenceCache
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