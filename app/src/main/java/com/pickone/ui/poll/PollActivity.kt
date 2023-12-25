package com.pickone.ui.poll

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.pickone.R
import com.pickone.data.Pack
import com.pickone.databinding.ActivityPollBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_PACK

@SuppressLint("ClickableViewAccessibility")
class PollActivity : BaseActivity() {

    private var _binding: ActivityPollBinding? = null
    private val binding: ActivityPollBinding
        get() = _binding!!

    private val viewModel: PollViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private var idTime: Long = -1
    private var startTime: Long = 0
    private var resultTime: Long = 0
    private val currentTime: Long
        get() = System.currentTimeMillis()

    override fun initView(args: Bundle) {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvTop.setOnClickListener {
            viewModel.onTopVote(elapsedTimeMs = stopTimer())
        }
        binding.tvBottom.setOnClickListener {
            viewModel.onBottomVote(elapsedTimeMs = stopTimer())
        }
        binding.tvNext.setOnClickListener { viewModel.onNextClick() }
        binding.tvStatistic.setOnClickListener { viewModel.onStatisticClick() }
        binding.tvRetry.setOnClickListener { viewModel.onRetryClick() }
        binding.tvHistory.setOnClickListener { viewModel.onPackHistoryClick() }

        binding.tvStatistic.isVisible = true
        binding.tvNext.isVisible = true

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        update(PollViewState.Loading)

        val input = intent.getParcelableExtra<Pack>(KEY_ARGS_PACK)
        if (input != null) {
            viewModel.onViewInitialized(input)
        } else {
            finish()
        }
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    override fun onStop() {
        super.onStop()
        pauseTimer()
    }

    override fun onStart() {
        super.onStart()
        playTimer()
    }

    private fun startTimer(id: Long) {
        idTime = id
        startTime = currentTime
        resultTime = 0
    }

    private fun pauseTimer() {
        resultTime += (currentTime - startTime)
    }

    private fun playTimer() {
        startTime = currentTime
    }

    private fun stopTimer() : Long {
        resultTime += (currentTime - startTime)
        val final = resultTime
        resultTime = 0
        return final
    }

    private fun update(state: PollViewState) {
        binding.vProgress.isVisible = state is PollViewState.Loading
        binding.vgContent.isVisible = state is PollViewState.Content
        binding.vgError.isVisible = state is PollViewState.Error
        binding.vgNoPolls.isVisible = state is PollViewState.Empty

        if (state !is PollViewState.Content) stopTimer()

        when(state) {
            is PollViewState.Empty -> {
                binding.tvPackTitle.text = state.packTitle
            }
            is PollViewState.Content -> {
                if (state.pollId != idTime) {
                    startTimer(state.pollId)
                }

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

    override fun onMovedToBackground() {
        super.onMovedToBackground()
        viewModel.onMovedToBackground()
    }


}