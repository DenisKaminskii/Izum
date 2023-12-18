package com.pickone.di

import android.content.Context
import com.pickone.api.custom.CustomPackApi
import com.pickone.api.PackApi
import com.pickone.api.PollApi
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.CustomPacksRepositoryImpl
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.data.repository.PublicPacksRepositoryImpl
import com.pickone.domain.core.PreferenceCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        @ApplicationContext context: Context,
        customPacksApi: CustomPackApi,
        preferenceCache: PreferenceCache
    ) : CustomPacksRepository {
        return CustomPacksRepositoryImpl(context, customPacksApi, preferenceCache)
    }

}