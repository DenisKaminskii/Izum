package com.pickone.domain.billing

import android.content.Context
import com.pickone.R
import com.pickone.data.repository.UserRepository
import com.pickone.network.OnUserIdUpdated
import com.pickone.ui.ViewAction
import com.pickone.ui.paywall.OnPremiumPurchased
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class BillingException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message) {

    object NotInitialized : BillingException("Attempting to use the Billing client that has not been initialized")

    data class NoSubscriptionFound(override val cause: Throwable? = null, override val message: String) : BillingException(message, cause)

}

sealed class PurchaseState {
    object Success : PurchaseState()
    data class Error(val message: String, val userCancelled: Boolean) : PurchaseState()
}

interface Billing {

    @Throws(BillingException::class)
    fun init()

    @Throws(BillingException::class)
    fun setUserId(userId: Long)

    @Throws(BillingException::class)
    suspend fun getWeeklySubscription(): StoreProduct

    @Throws(BillingException::class)
    suspend fun getLifetimeInApp(): StoreProduct

    @Throws(BillingException::class)
    suspend fun purchaseWeeklySubscription(
        onViewAction: (ViewAction) -> Unit
    ) : Flow<PurchaseState>

    @Throws(BillingException::class)
    suspend fun purchaseLifetimeInApp(
        onViewAction: (ViewAction) -> Unit
    ) : Flow<PurchaseState>

    fun checkSubscription()

    fun onPurchaseError(error: PurchasesError, userCancelled: Boolean)

    fun onPurchaseSuccess(storeTransaction: StoreTransaction?, customerInfo: CustomerInfo)

}


class BillingImpl(
    private val context: Context,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher,
    private val onUserIdUpdated: OnUserIdUpdated,
    private val onPremiumPurchased: OnPremiumPurchased
) : Billing,
    CoroutineScope by CoroutineScope(SupervisorJob() + ioDispatcher) {

    companion object {
        private const val WEEKLY_PACKAGE_ID = "$" + "rc_weekly"
        private const val LIFETIME_PACKAGE_ID = "$" + "rc_lifetime"

        private const val PREMIUM_ENTITLEMENT_ID = "com.polleo.app.premium"
    }

    private val apiKey: String
        get() = context.getString(R.string.revenue_cat_api_key)

    private val purchaseFlow = MutableSharedFlow<PurchaseState>()

    private var userIdUpdates: Job? = null

    private var weeklyPackage: Package? = null
    private var lifetimePackage: Package? = null

    private var isInitializedFailed = false

    override fun init() {
        try {
            isInitializedFailed = false

            // Purchases.logLevel = LogLevel.DEBUG //§PROD:
            val configuration = PurchasesConfiguration.Builder(context, apiKey).build()
            Purchases.configure(configuration)
            userRepository.userId?.let(::setUserId)

            Purchases.sharedInstance.updatedCustomerInfoListener =
                UpdatedCustomerInfoListener { customerInfo -> checkForSubscription(customerInfo) }

            onInitialized()
        } catch (e: Exception) {
            isInitializedFailed = true
            Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
            Timber.e(e, "RevenueCat init error")
        }
    }

    private fun onInitialized() = launch {
        subscribeToUserIdUpdates()
        checkSubscription()
        fetchWeeklyPackage()
        fetchLifetimePackage()
        restorePurchases()
    }

    private suspend fun fetchWeeklyPackage() {
        try {
            if (weeklyPackage == null) {
                weeklyPackage = getWeeklyPackage()
            }
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to fetch weekly package")
        }
    }

    private suspend fun fetchLifetimePackage() {
        try {
            if (lifetimePackage == null) {
                lifetimePackage = getLifetimePackage()
            }
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to fetch weekly package")
        }
    }

    private fun restorePurchases() {
        checkIsInitialized()

        Purchases.sharedInstance.restorePurchasesWith(
            onError = { error ->
                Timber.e("RevenueCat: restore purchases error: $error")
            },
            onSuccess = { customerInfo ->
                checkForSubscription(customerInfo)
            }
        )
    }

    override fun setUserId(userId: Long) {
        checkIsInitialized()

        Purchases.sharedInstance.logInWith(
            appUserID = userId.toString(),
            onError = { error ->
                Timber.e(Exception() ,"RevenueCat: set user id error: $error")
            },
            onSuccess = { purchaserInfo, created ->
                restorePurchases()
            })
    }

    override suspend fun getWeeklySubscription(): StoreProduct {
        return getWeeklyPackage().product
    }

    override suspend fun getLifetimeInApp(): StoreProduct {
        return getLifetimePackage().product
    }

    override suspend fun purchaseWeeklySubscription(
        onViewAction: (ViewAction) -> Unit
    ) : Flow<PurchaseState> {
        if (weeklyPackage == null) {
            fetchWeeklyPackage()
            if (weeklyPackage == null) throw BillingException.NoSubscriptionFound(message = "Can't purchase weekly subscription")
        }

        return purchase(
            weeklyPackage!!,
            onViewAction = onViewAction
        )
    }

    override suspend fun purchaseLifetimeInApp(
        onViewAction: (ViewAction) -> Unit
    ) : Flow<PurchaseState> {
        if (lifetimePackage == null) {
            fetchLifetimePackage()
            if (lifetimePackage == null) throw BillingException.NoSubscriptionFound(message = "Can't purchase lifetime subscription")
        }

        return purchase(
            lifetimePackage!!,
            onViewAction = onViewAction
        )
    }

    @Throws(BillingException::class)
    private fun purchase(
        pack: Package,
        onViewAction: (ViewAction) -> Unit
    ) : Flow<PurchaseState> {
        onViewAction.invoke(ViewAction.ShowPurchaseFlow(pack))
        return purchaseFlow
    }

    private suspend fun getWeeklyPackage(): Package = suspendCoroutine { continuation ->
        if (weeklyPackage != null) {
            continuation.resumeWith(Result.success(weeklyPackage!!))
            return@suspendCoroutine
        }

        checkIsInitialized()

        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                val message = error.message
                val exception = BillingException.NoSubscriptionFound(message = message)
                continuation.resumeWithException(exception)
            },
            onSuccess = { offerings ->
                val packages = offerings.current?.availablePackages
                if (packages.isNullOrEmpty()) {
                    val message = "No current offering available packages was found"
                    val exception = BillingException.NoSubscriptionFound(message = message)
                    continuation.resumeWithException(exception)
                } else {
                    val pack = packages.firstOrNull { it.identifier == WEEKLY_PACKAGE_ID }
                    if (pack != null) {
                        continuation.resumeWith(Result.success(pack))
                    } else {
                        val message = "No found $WEEKLY_PACKAGE_ID in ${packages.map { it.identifier }}"
                        val exception = BillingException.NoSubscriptionFound(message = message)
                        continuation.resumeWithException(exception)
                    }
                }
            }
        )
    }

    private suspend fun getLifetimePackage(): Package = suspendCoroutine { continuation ->
        if (lifetimePackage != null) {
            continuation.resumeWith(Result.success(lifetimePackage!!))
            return@suspendCoroutine
        }

        checkIsInitialized()

        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                val message = error.message
                val exception = BillingException.NoSubscriptionFound(message = message)
                continuation.resumeWithException(exception)
            },
            onSuccess = { offerings ->
                val packages = offerings.current?.availablePackages
                if (packages.isNullOrEmpty()) {
                    val message = "No current offering available packages was found"
                    val exception = BillingException.NoSubscriptionFound(message = message)
                    continuation.resumeWithException(exception)
                } else {
                    val pack = packages.firstOrNull { it.identifier == LIFETIME_PACKAGE_ID }
                    if (pack != null) {
                        continuation.resumeWith(Result.success(pack))
                    } else {
                        val message = "No found $LIFETIME_PACKAGE_ID in ${packages.map { it.identifier }}"
                        val exception = BillingException.NoSubscriptionFound(message = message)
                        continuation.resumeWithException(exception)
                    }
                }
            }
        )
    }

    override fun checkSubscription() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                Timber.e("RevenueCat: get customer info error: $error")
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                checkForSubscription(customerInfo)
            }
        })
    }

    override fun onPurchaseSuccess(
        storeTransaction: StoreTransaction?,
        customerInfo: CustomerInfo
    ) {
        checkForSubscription(customerInfo)
        launch {
            purchaseFlow.emit(PurchaseState.Success)
        }
    }

    override fun onPurchaseError(error: PurchasesError, userCancelled: Boolean) {
        launch {
            purchaseFlow.emit(PurchaseState.Error(error.message, userCancelled))
        }
    }

    private fun checkForSubscription(customerInfo: CustomerInfo) {
        val entitlement = customerInfo.entitlements[PREMIUM_ENTITLEMENT_ID]
        val hasSubscription = entitlement?.isActive == true
        userRepository.hasSubscription = hasSubscription
        if (hasSubscription) {
            launch { onPremiumPurchased.emit(Unit) }
        }
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


