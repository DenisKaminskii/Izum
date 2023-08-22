package com.izum.ui.create

import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.izum.databinding.ActivitySuggestPollBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggestPollActivity : BaseActivity() {

    private var _binding: ActivitySuggestPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SuggestPollViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivitySuggestPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView() {
        super.initView()

        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.vDone.setOnClickListener { viewModel.onDoneClick() }

        update(viewModel.viewState)

        viewModel.onViewInitialized(Unit)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
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
                binding.vDone.alpha = if (state.isDoneEnabled) 1f else 0.5f
            }
        }
    }

}