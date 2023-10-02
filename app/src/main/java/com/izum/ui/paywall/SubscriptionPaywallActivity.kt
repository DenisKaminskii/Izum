package com.izum.ui.paywall

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.izum.R
import com.izum.databinding.ActivitySubscriptionPaywallBinding
import com.izum.ui.BaseActivity
import com.izum.ui.dpF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class Price {
    WEEK,
    MONTH,
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

        iPriceFirst.setOnClickListener { update(Price.WEEK) }
        iPriceSecond.setOnClickListener { update(Price.MONTH) }
        iPriceThird.setOnClickListener { update(Price.LIFETIME) }

        update(Price.MONTH)
    }

    private fun update(price: Price) = with(binding) {
        iPriceFirst.update(
            PremiumPriceItemState(
                title = "Week",
                price = "5$",
                subtitle = "per week",
                isSelected = price == Price.WEEK,
                primaryColor = getColor(R.color.paywall_red),
                textColor = getColor(R.color.paywall_black)
            )
        )

        iPriceSecond.update(
            PremiumPriceItemState(
                title = "Month",
                price = "20$",
                subtitle = "per month",
                isSelected = price == Price.MONTH,
                primaryColor = getColor(R.color.paywall_purple),
                textColor = getColor(R.color.paywall_black)
            )
        )

        iPriceThird.update(
            PremiumPriceItemState(
                title = "LifeTime",
                price = "42$",
                subtitle = "forever",
                isSelected = price == Price.LIFETIME,
                primaryColor = getColor(R.color.paywall_green),
                textColor = getColor(R.color.paywall_black)
            )
        )
    }

}