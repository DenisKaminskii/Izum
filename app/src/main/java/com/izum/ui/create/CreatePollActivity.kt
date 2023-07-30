package com.izum.ui.create

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.databinding.ActivityCreatePollBinding
import com.izum.ui.BaseActivity
import com.izum.ui.poll.PollViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePollActivity : BaseActivity() {

    private var _binding: ActivityCreatePollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreatePollBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    private fun update(state: CreatePollViewState) {
        binding.vDone.isVisible = state is CreatePollViewState.Input
        binding.vLoading.isVisible = state is CreatePollViewState.Loading

        when (state) {
            is CreatePollViewState.Loading -> {
                // nothing
            }

            is CreatePollViewState.Input -> {
                binding.vDone.isEnabled = state.isDoneEnabled
            }
        }
    }

}