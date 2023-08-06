package com.izum.ui.packs

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.izum.R
import com.izum.databinding.ActivityPacksBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PacksActivity : BaseActivity() {

    private var _binding: ActivityPacksBinding? = null
    private val binding: ActivityPacksBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by viewModels()
    private val viewPagerAdapter: PacksViewPagerAdapter by lazy {
        PacksViewPagerAdapter(supportFragmentManager, lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPacksBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)
        initView()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                router.attachHost(this@PacksActivity)
                viewModel.routeFlow.collect {
                    router.route(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect {
                    updateViewState(it)
                }
            }
        }

        viewModel.init(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }

    private fun initView() {
        binding.vPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.vTabs, binding.vPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Official"
                1 -> "Custom"
                else -> ""
            }
        }.attach()
    }

    private fun updateViewState(state: PacksViewState) {

    }

}