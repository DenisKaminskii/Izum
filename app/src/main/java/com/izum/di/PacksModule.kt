package com.izum.di

import com.izum.api.PacksApi
import com.izum.data.packs.PacksRepository
import com.izum.data.packs.PacksRepositoryImpl
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
    ) : PacksRepository {
        return PacksRepositoryImpl(packsApi)
    }

}