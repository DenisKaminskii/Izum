package com.pickone.ui.paywall

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

interface OnPremiumPurchased : MutableSharedFlow<Unit>

class OnPremiumPurchasedImpl @Inject constructor() : OnPremiumPurchased,
    MutableSharedFlow<Unit> by MutableSharedFlow()