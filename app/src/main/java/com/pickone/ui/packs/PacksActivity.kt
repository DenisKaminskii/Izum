package com.pickone.ui.packs

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayoutMediator
import com.pickone.R
import com.pickone.analytics.Analytics
import com.pickone.databinding.ActivityPacksBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.create.PackTitleEditDialog
import com.revenuecat.purchases.Purchases
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PacksActivity : BaseActivity() {

    @Inject lateinit var analytics: Analytics

    private var _binding: ActivityPacksBinding? = null
    private val binding: ActivityPacksBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by viewModels()

    private val viewPagerAdapter: PacksViewPagerAdapter by lazy {
        PacksViewPagerAdapter(supportFragmentManager, lifecycle, ::onCreatePackClick)
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPacksBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black_top_bottom_bar)

        binding.vgPager.adapter = viewPagerAdapter

        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeClick() }
        binding.vCreatePack.setOnClickListener { onCreatePackClick() }
        binding.vSuggest.setOnClickListener { viewModel.onSuggestPollClick() }

        TabLayoutMediator(binding.vgTabs, binding.vgPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Feed"
                1 -> "My packs"
                2 -> "Info"
                else -> ""
            }
        }.attach()

        viewModel.onViewInitialized(Unit)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PacksViewState) {
        val hasSubscription = viewState.hasSubscription
        binding.tvSubscribe.isVisible = !hasSubscription
    }

    override fun onDestroy() {
        super.onDestroy()
        Purchases.sharedInstance.removeUpdatedCustomerInfoListener()
    }

    private var createPackDialog: PackTitleEditDialog? = null

    private fun onCreatePackClick() {
        analytics.customPackCreateTap()
        if (viewModel.isAvailableToCreateNewPack().not()) return

        createPackDialog?.dismissAllowingStateLoss()
        createPackDialog = null
        createPackDialog = PackTitleEditDialog.getInstance(
            title = "",
            onApply = { title ->
                viewModel.onCreatePack(title)
            },
            onCancel = {
                createPackDialog = null
            }
        )
        createPackDialog?.show(supportFragmentManager, PackTitleEditDialog::class.simpleName)
    }

}