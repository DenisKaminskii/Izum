package com.pickone.domain.billing

import android.content.Context
import android.util.Log
import com.pickone.R
import com.pickone.data.repository.UserRepository
import com.pickone.network.OnUserIdUpdated
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getProductsWith
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreProduct
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws

sealed class BillingException(
    override val message: String
) : Exception(message) {
    object NotInitialized : BillingException("Attempting to use the Billing client that has not been initialized")
    object NoSubscriptionFound : BillingException("No subscription found")
}

interface Billing {

    @Throws(BillingException::class)
    fun init()

    @Throws(BillingException::class)
    fun setUserId(userId: Long)

    @Throws(BillingException::class)
    suspend fun getWeeklySubscription(): StoreProduct

}


class BillingImpl(
    private val context: Context,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val onUserIdUpdated: OnUserIdUpdated
) : Billing,
    CoroutineScope by CoroutineScope(SupervisorJob() + ioDispatcher) {

    companion object {
        const val WEEKLY_SUBSCRIPTION_ID = "premium"
    }

    private val apiKey: String
        get() = context.getString(R.string.revenue_cat_api_key)

    private var userIdUpdates: Job? = null

    private var weeklySubscription: StoreProduct? = null

    var isInitializedFailed = false
        private set

    init {
        subscribeToUserIdUpdates()
    }

    override fun init() {
        try {
            isInitializedFailed = false

            Purchases.logLevel = LogLevel.DEBUG // §
            val configuration = PurchasesConfiguration.Builder(context, apiKey).build()
            Purchases.configure(configuration)

            onInitialized()
            Log.d("Steve", "RevenueCat: configure success with userId: ${userRepository.userId}")
        } catch (e: Exception) {
            isInitializedFailed = true
            Log.d("Steve", "RevenueCat: init error: $e")
        }
    }

    private fun onInitialized() = launch {
        subscribeToUserIdUpdates()
        fetchWeeklySubscription()
        // § check for subscription status
    }

    override fun setUserId(userId: Long) {
        checkIsInitialized()

        Purchases.sharedInstance.logInWith(
            appUserID = userId.toString(),
            onError = { error ->
                Log.d("Steve", "RevenueCat: set user id error: $error")
                /* Optional error handling */
            },
            onSuccess = { purchaserInfo, created ->
                Log.d("Steve", "RevenueCat: purchaserInfo: $purchaserInfo")
                /* Optional post success handling */
            })
    }

    private suspend fun fetchWeeklySubscription() {
        if (weeklySubscription != null) return
        weeklySubscription = getWeeklySubscription()
    }

    override suspend fun getWeeklySubscription(): StoreProduct = suspendCoroutine { continuation ->
        if (weeklySubscription != null) {
            continuation.resumeWith(Result.success(weeklySubscription!!))
            return@suspendCoroutine
        }

        checkIsInitialized()

        Purchases.sharedInstance.getProductsWith(
            productIds = listOf(WEEKLY_SUBSCRIPTION_ID),
            onError = { error ->
                Log.d("Steve", "RevenueCat: error: $error")
                continuation.resumeWithException(BillingException.NoSubscriptionFound)
            },
            onGetStoreProducts = { products ->
                Log.d("Steve", "RevenueCat: products: $products")
                val weeklyProduct = products.firstOrNull()
                if (weeklyProduct == null) {
                    continuation.resumeWithException(BillingException.NoSubscriptionFound)
                } else {
                    weeklySubscription = weeklyProduct
                    Log.d("Steve", "Weekly product is fetched!")
                    continuation.resumeWith(Result.success(weeklyProduct))
                }
            }
        )
    }

    @Throws(BillingException.NotInitialized::class)
    private fun checkIsInitialized() {
        if (isInitializedFailed) {
            init()
            if (isInitializedFailed) throw BillingException.NotInitialized
        }
    }

    private fun subscribeToUserIdUpdates() = launch {
        if (userIdUpdates?.isActive == true || isInitializedFailed) return@launch

        userIdUpdates?.cancelAndJoin()
        userIdUpdates = launch {
            onUserIdUpdated.collect(::setUserId)
        }
    }

}


