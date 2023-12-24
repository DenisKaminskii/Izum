package com.pickone.ui.paywall

import com.pickone.domain.billing.Billing
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction

interface PurchaseDelegate {

    fun onPurchaseError(error: PurchasesError, userCancelled: Boolean)

    fun onPurchaseSuccess(storeTransaction: StoreTransaction?, customerInfo: CustomerInfo)

}

class PurchaseDelegateImpl(
    private val billing: Billing
) : PurchaseDelegate {

    override fun onPurchaseError(error: PurchasesError, userCancelled: Boolean) {
        billing.onPurchaseError(error, userCancelled)
    }

    override fun onPurchaseSuccess(storeTransaction: StoreTransaction?, customerInfo: CustomerInfo) {
        billing.onPurchaseSuccess(storeTransaction, customerInfo)
    }

}