package com.izum.ui.packs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.izum.databinding.ActivityPacksBinding
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PacksActivity : ComponentActivity() {

    @Inject lateinit var router: Router

    private var _binding: ActivityPacksBinding? = null
    private val binding: ActivityPacksBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by viewModels()
    private val adapter = PacksAdapter { pack ->
        viewModel.onPackClick(pack)
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
                viewModel.uiStateFlow.collect {
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
        binding.vPacks.layoutManager = GridLayoutManager(this, 2)
        binding.vPacks.adapter = adapter
        binding.vCustom.setOnClickListener { viewModel.onCustomClick() }
    }

    private fun updateViewState(state: PacksViewState) {
        when(state) {
            is PacksViewState.Loading -> {
                binding.vProgress.isVisible = true
                binding.vPacks.isVisible = false
            }
            is PacksViewState.Packs -> {
                binding.vProgress.isVisible = false
                binding.vPacks.isVisible = true
                adapter.setItems(state.packs)
            }
        }
    }

}