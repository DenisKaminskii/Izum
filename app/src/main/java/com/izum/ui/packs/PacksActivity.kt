package com.izum.ui.packs

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
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
    }

    private fun initView() {
        binding.vgPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.vgTabs, binding.vgPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Official"
                1 -> "Custom"
                else -> ""
            }
        }.attach()

        viewModel.init(Unit)
    }

}