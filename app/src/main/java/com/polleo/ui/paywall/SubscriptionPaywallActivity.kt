package com.polleo.ui.paywall

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.polleo.R
import com.polleo.databinding.ActivitySubscriptionPaywallBinding
import com.polleo.ui.BaseActivity
import com.polleo.ui.dpF
import dagger.hilt.android.AndroidEntryPoint

private enum class Price {
    WEEK,
    LIFETIME
}

@AndroidEntryPoint
class SubscriptionPaywallActivity : BaseActivity() {

    private var _binding: ActivitySubscriptionPaywallBinding? = null
    private val binding get() = _binding!!

    override fun initLayout() {
        super.initLayout()
        _binding = ActivitySubscriptionPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) = with(binding) {
        super.initView(args)

        ivClose.setOnClickListener { finish() }

        iPriceFirst.setOnClickListener { update(Price.WEEK) }
        iPriceSecond.setOnClickListener { update(Price.LIFETIME) }

        update(Price.WEEK)
    }

    private fun update(price: Price) = with(binding) {
        val isWeekSelected = price == Price.WEEK
        iPriceFirst.update(
            PremiumPriceItemState(
                title = "Weekly / 5$",
                subtitle = "Try 3 days free, then 5$ per week",
                isSelected = isWeekSelected,
                titleColor = if (isWeekSelected) getColor(R.color.white) else getColor(R.color.paywall_pink),
                subtitleColor = if (isWeekSelected) getColor(R.color.white_95) else getColor(R.color.paywall_grey),
                icon = if (isWeekSelected) R.drawable.ic_paywall_subscribe_checked else R.drawable.ic_paywall_unchecked,
                accentColor = getColor(R.color.paywall_pink)
            )
        )

        val isLifetimeSelected = price == Price.LIFETIME
        iPriceSecond.update(
            PremiumPriceItemState(
                title = "Lifetime 38$",
                subtitle = "Paid once!",
                isSelected = isLifetimeSelected,
                titleColor = if (isLifetimeSelected) getColor(R.color.black) else getColor(R.color.paywall_green),
                subtitleColor = if (isLifetimeSelected) getColor(R.color.black_95) else getColor(R.color.paywall_grey),
                icon = if (isLifetimeSelected) R.drawable.ic_paywall_lifetime_checked else R.drawable.ic_paywall_unchecked,
                accentColor = getColor(R.color.paywall_green)
            )
        )

        with(tvSubscribe) {
            background = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(
                    getColor(if (isWeekSelected) R.color.paywall_gradient_week_start else R.color.paywall_gradient_lifetime_start),
                    getColor(if (isWeekSelected) R.color.paywall_gradient_week_end else R.color.paywall_gradient_lifetime_end)
                )
            ).apply {
                cornerRadius = dpF(8)
            }

            text = if (isWeekSelected) "TRY FREE & SUBSCRIBE" else "PURCHASE"
            setTextColor(getColor(if (isWeekSelected) R.color.white else R.color.black))
        }

        tvSubscribeTitle.text =
            if (isWeekSelected) "You have a free 3 days! \uD83E\uDD73\uD83D\uDE4C"
            else "No further charges! \uD83D\uDE4C"
        tvSubscribeSubtitle.text =
            if (isWeekSelected) "After 02.09 the subscription price will be 5\$ / month"
            else "Always-on access to all content"
    }

}