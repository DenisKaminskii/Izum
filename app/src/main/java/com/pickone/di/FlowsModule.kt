package com.pickone.di

import com.pickone.network.OnUserIdUpdated
import com.pickone.network.OnUserIdUpdatedImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface FlowsModule {

    @Binds
    @Singleton
    fun bindOnUserIdUpdatedFlow(impl: OnUserIdUpdatedImpl): OnUserIdUpdated

}