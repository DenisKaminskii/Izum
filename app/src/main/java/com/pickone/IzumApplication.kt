package com.pickone

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getOfferingsWith
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IzumApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Purchases.logLevel = LogLevel.DEBUG // §
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this,
                getString(R.string.revenue_cat_api_key)
            ).build()
        )

        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                /* Optional error handling */
            },
            onSuccess = { offerings ->
                // Display current offering with offerings.current

            }
        )


    }

}