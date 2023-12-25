package com.pickone.ui.paywall

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.pickone.R
import com.pickone.analytics.Analytics
import com.pickone.databinding.ActivityPaywallBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_INPUT
import com.pickone.ui.LoadingDialog
import com.pickone.ui.dpF
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class PaywallInput(
    val fromOnboarding: Boolean
) : Parcelable

@AndroidEntryPoint
class PaywallActivity : BaseActivity() {

    @Inject lateinit var analytics: Analytics

    private var _binding: ActivityPaywallBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaywallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) = with(binding) {
        super.initView(args)
        ivClose.setOnClickListener {
            analytics.paywallClosed()
            finish()
        }

        iPriceFirst.setOnClickListener { viewModel.onWeeklyOptionSelected() }
        // § iPriceSecond.setOnClickListener { viewModel.onLifetimeOptionSelected() }
        iPriceSecond.update(
            PremiumPriceItemState(
                title = "Lifetime",
                subtitle = "Will be soon ..",
                isSelected = false,
                titleColor = getColor(R.color.paywall_green_dark),
                subtitleColor = getColor(R.color.white_25),
                icon = null,
                accentColor = getColor(R.color.paywall_green_dark)
            )
        )
        tvSubscribe.setOnClickListener { viewModel.onPurchaseClicked() }

        onBackPressedDispatcher.addCallback(
            this@PaywallActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                }
            })

        viewModel.onViewInitialized(
            input = args.getParcelable(KEY_ARGS_INPUT)!!
        )
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
        // iPriceSecond.update(viewState.lifetimeItem)

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

        val isLoadingDialogVisibleNow = loadingDialog?.isAdded == true
        if (viewState.isShowDelegateLoading && !isLoadingDialogVisibleNow) {
            showLoadingDialog()
        }

        if (!viewState.isShowDelegateLoading && isLoadingDialogVisibleNow) {
            hideLoadingDialog()
        }
    }

    override fun onPurchaseSuccess(storeTransaction: StoreTransaction?, customerInfo: CustomerInfo) {
        super.onPurchaseSuccess(storeTransaction, customerInfo)
        viewModel.onPurchaseSuccess(storeTransaction, customerInfo)
    }

    override fun onPurchaseError(error: PurchasesError, userCancelled: Boolean) {
        super.onPurchaseError(error, userCancelled)
        viewModel.onPurchaseError(error, userCancelled)
    }

    private var loadingDialog: LoadingDialog? = null

    private fun showLoadingDialog() {
        loadingDialog?.dismissAllowingStateLoss()
        loadingDialog = null
        loadingDialog = LoadingDialog.getInstance()
        loadingDialog?.show(supportFragmentManager, LoadingDialog::class.simpleName)
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismissAllowingStateLoss()
        loadingDialog = null
    }

}