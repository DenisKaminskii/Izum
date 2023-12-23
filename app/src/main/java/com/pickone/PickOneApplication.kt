package com.pickone

import android.app.Application
import com.pickone.domain.billing.Billing
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PickOneApplication : Application() {

    @Inject lateinit var billing: Billing

    override fun onCreate() {
        super.onCreate()
        initBilling()
    }

    private fun initBilling() {
        billing.init()
    }

}