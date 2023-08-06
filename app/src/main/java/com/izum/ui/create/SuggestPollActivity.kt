package com.izum.ui.create

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.databinding.ActivitySuggestPollBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SuggestPollActivity : BaseActivity() {

    private var _binding: ActivitySuggestPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuggestPollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySuggestPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
        update(viewModel.viewState)

        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.vDone.setOnClickListener { viewModel.onDoneClick() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewActionsFlow.collect { action -> accept(action) }
            }
        }

        viewModel.init(Unit)
    }

    private fun update(state: SuggestPollViewState) {
        binding.vDone.isVisible = state is SuggestPollViewState.Input
        binding.vLoading.isVisible = state is SuggestPollViewState.Loading

        when (state) {
            is SuggestPollViewState.Loading -> {
                // nothing
            }

            is SuggestPollViewState.Input -> {
                binding.vDone.isEnabled = state.isDoneEnabled
            }
        }
    }

}