package com.pickone.ui.paywall

import android.content.Context
import androidx.annotation.ColorInt
import androidx.lifecycle.viewModelScope
import com.pickone.R
import com.pickone.data.repository.UserRepository
import com.pickone.domain.billing.Billing
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.route.Router
import com.revenuecat.purchases.models.StoreProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Price {
    WEEK,
    LIFETIME
}

sealed class PaywallViewState {
    object Loading : PaywallViewState()
    data class Content(
        val weeklyItem: PremiumPriceItemState,
        val lifetimeItem: PremiumPriceItemState,
        val extraTitle: String,
        val extraSubtitle: String?,
        @ColorInt
        val buttonGradientStart: Int,
        @ColorInt
        val buttonGradientEnd: Int,
        val buttonText: String,
        @ColorInt
        val buttonTextColor: Int
    ) : PaywallViewState()
}

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: Billing,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : StateViewModel<Unit, PaywallViewState>(
    initialState = PaywallViewState.Loading
) {

    private var selectedPrice = Price.WEEK

    private var weeklySubscription: StoreProduct? = null

    override fun onViewInitialized(input: Unit) {
        super.onViewInitialized(input)

        if (userRepository.hasSubscription)  {
            viewModelScope.launch {
                route(Router.Route.Finish)
            }
            return
        }

        fetchProducts()
    }

    private fun fetchProducts() = viewModelScope.launch {
        weeklySubscription = billing.getWeeklySubscription()
        // §TODO: get in app
        updateView()
    }

    private fun updateView() {
        val weekly = weeklySubscription ?: return

        val isWeeklySelected = selectedPrice == Price.WEEK
        val isLifetimeSelected = selectedPrice == Price.LIFETIME
        var isTrialAvailable = true // §

        updateState {
            PaywallViewState.Content(
                weeklyItem = PremiumPriceItemState(
                    title = "Weekly / ${weekly.price.formatted}",
                    subtitle = "Try 3 days free, then ${weekly.price.formatted} per week",
                    isSelected = isWeeklySelected,
                    titleColor = if (isWeeklySelected) context.getColor(R.color.white_95) else context.getColor(R.color.paywall_pink),
                    subtitleColor = if (isWeeklySelected) context.getColor(R.color.white_75) else context.getColor(R.color.paywall_grey),
                    icon = if (isWeeklySelected) R.drawable.ic_paywall_subscribe_checked else R.drawable.ic_paywall_unchecked,
                    accentColor = context.getColor(R.color.paywall_pink)
                ),
                lifetimeItem = PremiumPriceItemState(
                    title = "Lifetime 50$",
                    subtitle = "One time payment",
                    isSelected = isLifetimeSelected,
                    titleColor = if (isLifetimeSelected) context.getColor(R.color.black) else context.getColor(R.color.paywall_green),
                    subtitleColor = if (isLifetimeSelected) context.getColor(R.color.black_95) else context.getColor(R.color.paywall_grey),
                    icon = if (isLifetimeSelected) R.drawable.ic_paywall_lifetime_checked else R.drawable.ic_paywall_unchecked,
                    accentColor = context.getColor(R.color.paywall_green)
                ),
                extraTitle = when (selectedPrice) {
                    Price.WEEK -> if (isTrialAvailable) {
                        "You have a free 3 days! \uD83E\uDD73\uD83D\uDE4C"
                    } else {
                        "Access to all content \uD83D\uDCAB"
                    }
                    Price.LIFETIME -> "No further charges! \uD83D\uDE4C"
                },
                extraSubtitle = when (selectedPrice) {
                    Price.WEEK -> if (isTrialAvailable) {
                        "After 3 days you will be charged ${weekly.price.formatted} per week"
                    } else {
                        null
                    }
                    Price.LIFETIME -> "Always-on access to all content"
                },
                buttonGradientStart = context.getColor(if (isWeeklySelected) R.color.paywall_gradient_week_start else R.color.paywall_gradient_lifetime_start),
                buttonGradientEnd = context.getColor(if (isWeeklySelected) R.color.paywall_gradient_week_end else R.color.paywall_gradient_lifetime_end),
                buttonText = when (selectedPrice) {
                    Price.WEEK -> if (isTrialAvailable) {
                        "TRY FREE & SUBSCRIBE"
                    } else {
                        "SUBSCRIBE"
                    }
                    Price.LIFETIME -> "PURCHASE"
                },
                buttonTextColor = context.getColor(if (isWeeklySelected) R.color.white else R.color.black)
            )
        }
    }

    fun onWeeklyOptionSelected() {
        selectedPrice = Price.WEEK
        updateView()
    }

    fun onLifetimeOptionSelected() {
        selectedPrice = Price.LIFETIME
        updateView()
    }

    fun onPurchaseClicked() {
        // §TODO:
    }

}