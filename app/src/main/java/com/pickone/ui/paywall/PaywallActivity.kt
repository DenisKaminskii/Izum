package com.pickone.ui.paywall

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.pickone.databinding.ActivityPaywallBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.dpF
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaywallActivity : BaseActivity() {

    private var _binding: ActivityPaywallBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaywallViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) = with(binding) {
        super.initView(args)
        ivClose.setOnClickListener { finish() }

        iPriceFirst.setOnClickListener { viewModel.onWeeklyOptionSelected() }
        iPriceSecond.setOnClickListener { viewModel.onLifetimeOptionSelected() }
        tvSubscribe.setOnClickListener { viewModel.onPurchaseClicked() }

        viewModel.onViewInitialized(Unit)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PaywallViewState) = with(binding) {
        vLoading.isVisible = viewState is PaywallViewState.Loading
        vgContent.isVisible = viewState is PaywallViewState.Content

        if (viewState !is PaywallViewState.Content) return@with

        iPriceFirst.update(viewState.weeklyItem)
        iPriceSecond.update(viewState.lifetimeItem)

        with(tvSubscribe) {
            background = GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                intArrayOf(viewState.buttonGradientStart, viewState.buttonGradientEnd)
            ).apply {
                cornerRadius = dpF(14)
            }

            text = viewState.buttonText
            setTextColor(viewState.buttonTextColor)
        }

        tvSubscribeTitle.text = viewState.extraTitle

        tvSubscribeSubtitle.isVisible = viewState.extraSubtitle != null
        tvSubscribeSubtitle.text = viewState.extraSubtitle
    }

}