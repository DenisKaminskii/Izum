package com.pickone.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.pickone.databinding.ActivityMainBinding
import com.pickone.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        super.initView(args)

        binding.tvReload.setOnClickListener { viewModel.onReloadClick() }

        viewModel.onViewInitialized(Unit)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    fun update(state: MainViewState) {
        binding.vProgress.isVisible = state.isLoading
        binding.vgReload.isVisible = !state.isLoading
    }

}