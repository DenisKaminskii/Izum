package com.polleo.di

import com.polleo.api.custom.CustomPackApi
import com.polleo.api.PackApi
import com.polleo.api.PollApi
import com.polleo.data.repository.CustomPacksRepository
import com.polleo.data.repository.CustomPacksRepositoryImpl
import com.polleo.data.repository.PublicPacksRepository
import com.polleo.data.repository.PublicPacksRepositoryImpl
import com.polleo.domain.core.PreferenceCache
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
        pollsApi: PollApi,
        preferenceCache: PreferenceCache
    ) : PublicPacksRepository {
        return PublicPacksRepositoryImpl(packsApi, pollsApi, preferenceCache)
    }

    @Provides
    @Singleton
    fun provideCustomPacksRepository(
        customPacksApi: CustomPackApi,
        preferenceCache: PreferenceCache
    ) : CustomPacksRepository {
        return CustomPacksRepositoryImpl(customPacksApi, preferenceCache)
    }

}