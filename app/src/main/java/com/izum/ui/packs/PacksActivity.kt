package com.izum.ui.packs

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.izum.R
import com.izum.data.repository.UserRepository
import com.izum.databinding.ActivityPacksBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PacksActivity : BaseActivity() {

    @Inject lateinit var userRepository: UserRepository

    private var _binding: ActivityPacksBinding? = null
    private val binding: ActivityPacksBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by viewModels()

    private val viewPagerAdapter: PacksViewPagerAdapter by lazy {
        PacksViewPagerAdapter(supportFragmentManager, lifecycle)
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPacksBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView() {
        binding.vgPager.adapter = viewPagerAdapter
        binding.vgSubscription.setOnClickListener {
            viewModel.onSubscribeClick()
        }

        TabLayoutMediator(binding.vgTabs, binding.vgPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Official"
                1 -> "Custom"
                else -> ""
            }
        }.attach()

        update(PacksViewState.Loading)

        viewModel.onViewInitialized(Unit)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PacksViewState) {
        when(viewState) {
            is PacksViewState.Loading -> {
                // ignore
            }
            is PacksViewState.Packs -> {
                val hasSubscription = viewState.hasSubscription
                binding.tvSubscription.text = if (hasSubscription) "Subscribed" else "Not subscribed"
                binding.ivSubscription.setImageResource(if (hasSubscription) R.drawable.ic_done_24 else R.drawable.ic_stars_24)
            }
        }
    }

}