package com.pickone.ui.paywall

import android.content.Context
import android.util.Log
import androidx.annotation.ColorInt
import androidx.lifecycle.viewModelScope
import com.pickone.R
import com.pickone.data.repository.UserRepository
import com.pickone.domain.billing.Billing
import com.pickone.domain.billing.BillingException
import com.pickone.domain.billing.PurchaseState
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.ViewAction
import com.pickone.ui.route.Router
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
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
        val buttonTextColor: Int,
        val isShowDelegateLoading: Boolean
    ) : PaywallViewState()
}

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billing: Billing,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : StateViewModel<Unit, PaywallViewState>(
    initialState = PaywallViewState.Loading
), PurchaseDelegate by PurchaseDelegateImpl(billing) {

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
        weeklySubscription = tryWithBilling { billing.getWeeklySubscription() }
        // §TODO: get in app
        updateView()
    }

    private fun updateView() {
        val weekly = weeklySubscription ?: return

        val isWeeklySelected = selectedPrice == Price.WEEK
        val isLifetimeSelected = selectedPrice == Price.LIFETIME
        val isTrialAvailable = weekly.subscriptionOptions?.freeTrial != null

        updateState { prevState ->
            PaywallViewState.Content(
                weeklyItem = PremiumPriceItemState(
                    title = "Weekly / ${weekly.price.formatted}",
                    subtitle = if (isTrialAvailable) {
                        "Try 3 days free, then ${weekly.price.formatted} per week"
                    } else {
                        "Subscription"
                    },
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
                buttonTextColor = context.getColor(if (isWeeklySelected) R.color.white else R.color.black),
                isShowDelegateLoading = if (prevState is PaywallViewState.Content) {
                    prevState.isShowDelegateLoading
                } else {
                    false
                }
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

    fun onPurchaseClicked() = viewModelScope.launch {
        showDelegateLoading()

        tryWithBilling {
            billing.purchaseWeeklySubscription(
                onViewAction = { viewAction ->
                    viewModelScope.launch {
                        emit(viewAction)
                    }
                }
            )
                .collect { purchaseState ->
                    when (purchaseState) {
                        is PurchaseState.Success -> {
                            emit(ViewAction.ShowToast("Enjoy your premium! \uD83E\uDD70"))
                            route(Router.Route.Finish)
                        }

                        is PurchaseState.Error -> {
                            Log.d("Steve", "RevenueCat: error: ${purchaseState.message}")
                            emit(ViewAction.ShowToast("Purchase declined :("))
                            hideDelegateLoading()
                        }

                        is PurchaseState.Canceled -> {
                            hideDelegateLoading()
                        }
                    }

                }
        }
    }

    private fun showDelegateLoading() {
        updateState { state ->
            if (state is PaywallViewState.Content) {
                state.copy(isShowDelegateLoading = true)
            } else {
                state
            }
        }
    }

    private fun hideDelegateLoading() {
        updateState { state ->
            if (state is PaywallViewState.Content) {
                state.copy(isShowDelegateLoading = false)
            } else {
                state
            }
        }
    }

    private suspend fun <T> tryWithBilling(
        action: suspend () -> T
    ) : T? {
        return try {
            action.invoke()
        } catch (exception: BillingException.NotInitialized) {
            Log.d("Steve", "RevenueCat: error: $exception")
            emit(ViewAction.ShowToast("Unable to connect with Google services :( \n" +
                    "Try again later \uD83D\uDE4F"))
            route(Router.Route.Finish)
            null
        } catch (exception: BillingException.NoSubscriptionFound) {
            Log.d("Steve", "RevenueCat: error: $exception")
            emit(ViewAction.ShowToast("Unable to connect with Google services :( \n" +
                    "Try again later \uD83D\uDE4F"))
            route(Router.Route.Finish)
            null
        } catch (exception: Exception) {
            Log.d("Steve", "RevenueCat: error: $exception")
            emit(ViewAction.ShowToast("Something went wrong :( \n" +
                    "Try again later \uD83D\uDE4F"))
            route(Router.Route.Finish)
            null
        }
    }

}