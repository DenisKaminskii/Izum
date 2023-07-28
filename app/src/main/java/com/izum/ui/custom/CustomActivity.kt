package com.izum.ui.custom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.izum.databinding.ActivityCustomBinding
import com.izum.ui.packs.PacksAdapter
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CustomActivity : ComponentActivity() {

    @Inject lateinit var router: Router

    private var _binding: ActivityCustomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomViewModel by viewModels()
    private val adapter = PacksAdapter { pack ->
        viewModel.onPackClick(pack)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                router.attachHost(this@CustomActivity)
                viewModel.routeFlow.collect {
                    router.route(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateFlow.collect {
                    updateViewState(it)
                }
            }
        }

        initView()
        viewModel.init(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }

    private fun initView() {
        binding.vPacks.layoutManager = GridLayoutManager(this, 2)
        binding.vPacks.adapter = adapter
        binding.vBack.setOnClickListener { viewModel.onBackClick() }
    }

    private fun updateViewState(state: CustomViewState) {
        when(state) {
            is CustomViewState.Loading -> {
                binding.vProgress.isVisible = true
                binding.vPacks.isVisible = false
            }
            is CustomViewState.Packs -> {
                binding.vProgress.isVisible = false
                binding.vPacks.isVisible = true
                adapter.setItems(state.packs)
            }
        }
    }

}