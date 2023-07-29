package com.izum.di

import com.izum.api.PacksApi
import com.izum.data.repository.PacksRepository
import com.izum.data.repository.PacksRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PacksModule {

    @Provides
    @Singleton
    fun providePacksRepository(
        packsApi: PacksApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ) : PacksRepository {
        return PacksRepositoryImpl(packsApi, ioDispatcher)
    }

}