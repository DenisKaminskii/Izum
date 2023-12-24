package com.pickone.ui

import com.revenuecat.purchases.Package

sealed interface ViewAction {
    data class ShowToast(val message: String) : ViewAction
    data class ShowPurchaseFlow(val pack: Package) : ViewAction
}