package com.pickone

import android.app.Application
import com.pickone.data.repository.UserRepository
import com.pickone.domain.billing.Billing
import com.pickone.log.CrashReportingTree
import com.pickone.log.PickoneUncaughtExceptionHandler
import com.pickone.network.NetworkMonitor
import com.revenuecat.purchases.Purchases
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class PickOneApplication : Application() {

    @Inject lateinit var billing: Billing
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()
        initBilling()
        auth()
        checkSubscription()
        initTimber()
    }

    private fun initBilling() {
        billing.init()

        GlobalScope.launch {
            networkMonitor.isOnline
                .collect { isOnline ->
                    if (isOnline && !Purchases.isConfigured) {
                        billing.init()
                    }
                }
        }
    }

    private fun initTimber() {
        Timber.plant(if (false) { //§TODO: Release important
            Timber.DebugTree()
        } else {
            CrashReportingTree()
        })

        Thread.setDefaultUncaughtExceptionHandler(
            PickoneUncaughtExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()
            )
        )
    }

    private fun auth() = GlobalScope.launch {
        userRepository.auth()
    }

    private fun checkSubscription() {
        billing.checkSubscription()
    }

}