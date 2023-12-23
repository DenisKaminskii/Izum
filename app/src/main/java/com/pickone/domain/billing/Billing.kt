package com.pickone.domain.billing

import android.content.Context
import android.util.Log
import com.pickone.R
import com.pickone.data.repository.UserRepository
import com.pickone.network.OnUserIdUpdated
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.logInWith
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

interface Billing {

    fun init()

    fun setUserId(userId: Long)

}


class BillingImpl(
    private val context: Context,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val onUserIdUpdated: OnUserIdUpdated
) : Billing,
    CoroutineScope by CoroutineScope(SupervisorJob() + ioDispatcher) {

    private val apiKey: String
        get() = context.getString(R.string.revenue_cat_api_key)

    private var userIdUpdates: Job? = null

    init {
        subscribeToUserIdUpdates()
    }

    override fun init() {
        try {
            Purchases.logLevel = LogLevel.DEBUG // §
            Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
            subscribeToUserIdUpdates()
            Log.d("Steve", "RevenueCat: configure success with userId: ${userRepository.userId}")
        } catch (e: Exception) {
            Log.d("Steve", "RevenueCat: init error: $e")
        }
    }

    override fun setUserId(userId: Long) = ifInitialized {
        Purchases.sharedInstance.logInWith(
            appUserID = userId.toString(),
            onError = { error ->
                Log.d("Steve", "RevenueCat: error: $error")
                /* Optional error handling */
            },
            onSuccess = { purchaserInfo, created ->
                Log.d("Steve", "RevenueCat: purchaserInfo: $purchaserInfo")
                /* Optional post success handling */
            })
    }

    private inline fun ifInitialized(action: () -> Unit) {
        if (Purchases.isConfigured) {
            action.invoke()
        } else {
            init()
            if (Purchases.isConfigured) {
                action.invoke()
            } else {
                Log.d("Steve", "RevenueCat: not configured")
            }
        }
    }

    private fun subscribeToUserIdUpdates() = launch {
        if (userIdUpdates?.isActive == true) return@launch

        userIdUpdates?.cancelAndJoin()
        userIdUpdates = launch {
            onUserIdUpdated.collect { userId ->
                setUserId(userId)
            }
        }
    }

}

//Purchases.sharedInstance.getProductsWith(
//productIds = listOf("premium"),
//onError = { error ->
//    Log.d("Steve", "RevenueCat: error: $error")
//    /* Optional error handling */
//},
//onGetStoreProducts = { products ->
//    Log.d("Steve", "RevenueCat: products: $products")
//    /* Optional post success handling */
//}
//)
