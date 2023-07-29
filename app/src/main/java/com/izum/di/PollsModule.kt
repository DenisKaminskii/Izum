package com.izum.di

import com.izum.api.PollsApi
import com.izum.data.repository.PollsRepository
import com.izum.data.repository.PollsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PollsModule {

    @Provides
    @Singleton
    fun providePollsRepository(
        pollsApi: PollsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): PollsRepository {
        return PollsRepositoryImpl(pollsApi, ioDispatcher)
    }

}