package com.izum.di

import com.izum.api.custom.CustomPackApi
import com.izum.api.PackApi
import com.izum.api.PollApi
import com.izum.data.repository.CustomPacksRepository
import com.izum.data.repository.CustomPacksRepositoryImpl
import com.izum.data.repository.PublicPacksRepository
import com.izum.data.repository.PublicPacksRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PacksModule {

    @Provides
    @Singleton
    fun providePacksRepository(
        packsApi: PackApi,
        pollsApi: PollApi
    ) : PublicPacksRepository {
        return PublicPacksRepositoryImpl(packsApi, pollsApi)
    }

    @Provides
    @Singleton
    fun provideCustomPacksRepository(
        customPacksApi: CustomPackApi,
    ) : CustomPacksRepository {
        return CustomPacksRepositoryImpl(customPacksApi)
    }

}