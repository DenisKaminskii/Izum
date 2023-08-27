package com.izum.di

import com.izum.api.CustomPacksApi
import com.izum.api.PacksApi
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
        packsApi: PacksApi
    ) : PublicPacksRepository {
        return PublicPacksRepositoryImpl(packsApi)
    }

    @Provides
    @Singleton
    fun provideCustomPacksRepository(
        customPacksApi: CustomPacksApi,
    ) : CustomPacksRepository {
        return CustomPacksRepositoryImpl(customPacksApi)
    }

}