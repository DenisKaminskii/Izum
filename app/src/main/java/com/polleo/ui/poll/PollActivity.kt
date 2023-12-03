package com.polleo.ui.poll

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.polleo.R
import com.polleo.databinding.ActivityPollBinding
import com.polleo.ui.BaseActivity

@SuppressLint("ClickableViewAccessibility")
class PollActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_PACK_ID = "KEY_ARGS_PACK_ID"
        const val KEY_ARGS_PACK_TITLE = "KEY_ARGS_PACK_TITLE"
    }

    private var _binding: ActivityPollBinding? = null
    private val binding: ActivityPollBinding
        get() = _binding!!

    private val viewModel: PollViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvTop.setOnClickListener { viewModel.onTopVote() }
        binding.tvBottom.setOnClickListener { viewModel.onBottomVote() }
        binding.tvNext.setOnClickListener { viewModel.onNextClick() }
        binding.tvStatistic.setOnClickListener { viewModel.onStatisticClick() }
        binding.tvRetry.setOnClickListener { viewModel.onRetryClick() }
        binding.tvHistory.setOnClickListener { viewModel.onPackHistoryClick() }

        binding.tvStatistic.isVisible = true
        binding.tvNext.isVisible = true

        update(PollViewState.Loading)

        viewModel.onViewInitialized(
            input = PollViewModel.Companion.Arguments(
                packId = intent.getLongExtra(KEY_ARGS_PACK_ID, -1),
                packTitle = intent.getStringExtra(KEY_ARGS_PACK_TITLE) ?: ""
            )
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(state: PollViewState) {
        binding.vProgress.isVisible = state is PollViewState.Loading
        binding.vgContent.isVisible = state is PollViewState.Content
        binding.vgError.isVisible = state is PollViewState.Error
        binding.vgNoPolls.isVisible = state is PollViewState.Empty

        when(state) {
            is PollViewState.Empty -> {
                binding.tvPackTitle.text = state.packTitle
            }
            is PollViewState.Content -> {
                val isTopVoted = state.votedOptionId == state.top.id
                val isBottomVoted = state.votedOptionId == state.bottom.id
                val isVoted = isTopVoted || isBottomVoted

                binding.tvPackTitle.text = state.packTitle
                binding.vgTop.alpha = when (state.votedOptionId) {
                    state.top.id -> 1f
                    state.bottom.id -> 0.69f
                    else -> 1f
                }
                binding.tvTop.text = state.top.title
                binding.tvTopCount.isVisible = isVoted
                binding.tvTopCount.text = state.top.votesText

                binding.vgBottom.alpha = when (state.votedOptionId) {
                    state.top.id -> 0.69f
                    state.bottom.id -> 1f
                    else -> 1f
                }
                binding.tvBottom.text = state.bottom.title
                binding.tvBottomCount.isVisible = isVoted
                binding.tvBottomCount.text = state.bottom.votesText

                binding.tvStatistic.isEnabled = isVoted
                binding.tvNext.isEnabled = isVoted

                if (state.isFinishVisible) {
                    binding.tvNext.setCompoundDrawables(null, null, null, null)
                    binding.tvNext.text = getString(R.string.finish)
                }
            }
            else -> {}
        }
    }


}