package com.pickone.di

import android.content.Context
import com.pickone.data.repository.UserRepository
import com.pickone.domain.billing.Billing
import com.pickone.domain.billing.BillingImpl
import com.pickone.network.OnUserIdUpdated
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BillingModule {

    @Provides
    @Singleton
    fun provideBilling(
        @ApplicationContext context: Context,
        userRepository: UserRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        onUserIdUpdated: OnUserIdUpdated
    ): Billing {
        return BillingImpl(
            context,
            userRepository,
            ioDispatcher,
            onUserIdUpdated
        )
    }

}