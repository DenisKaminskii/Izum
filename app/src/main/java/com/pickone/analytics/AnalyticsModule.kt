package com.pickone.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface AnalyticsModule {

    @Binds
    @Singleton
    fun bindAnalytics(
        firebaseAnalytics: FirebaseAnalytics
    ): Analytics

}