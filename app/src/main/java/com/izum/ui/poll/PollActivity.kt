package com.izum.ui.poll

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.izum.R
import com.izum.databinding.ActivityPollBinding
import com.izum.ui.BaseActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvTop.setOnClickListener { viewModel.onTopVote() }
        binding.tvBottom.setOnClickListener { viewModel.onBottomVote() }
        binding.ivNext.setOnClickListener { viewModel.onNextClick() }
        binding.tvStatistic.setOnClickListener { viewModel.onStatisticClick() }

//         binding.tvTop.background = getBackgroundGradient(color = getColor(R.color.red))
//         binding.tvBottom.background = getBackgroundGradient(color = getColor(R.color.sand))

        update(PollViewState.Loading)

        viewModel.onViewInitialized(
            args = PollViewModel.Companion.Arguments(
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

    private fun View.animateShow() = lifecycleScope.launch {
        this@animateShow.alpha = 0f
        this@animateShow.isVisible = true
        this@animateShow.animate()
            .alpha(1f)
            .setDuration(350)
            .start()
    }

    private fun View.animateHide() = lifecycleScope.launch {
        animate()
            .alpha(0f)
            .setDuration(350)
            .start()

        withContext(IO) {
            delay(350)
            withContext(Main) {
                isVisible = false
            }
        }
    }

    private fun update(state: PollViewState) {
        binding.vProgress.isVisible = state is PollViewState.Loading
        binding.vgContent.isVisible = state !is PollViewState.Loading

        if (state !is PollViewState.Poll) return

        val isTopVoted = state.votedOptionId == state.top.id
        val isBottomVoted = state.votedOptionId == state.bottom.id
        val isVoted = isTopVoted || isBottomVoted
        val topCount = state.top.votesCount + (if (isTopVoted) 1 else 0)
        val bottomCount = state.bottom.votesCount + (if (isBottomVoted) 1 else 0)
        val allCount = topCount + bottomCount

        binding.tvPackTitle.text = state.packTitle

        binding.vgTop.alpha = when (state.votedOptionId) {
            state.top.id -> 1f
            state.bottom.id -> 0.69f
            else -> 1f
        }
        binding.tvTop.text = state.top.title
        binding.tvTopCount.isVisible = isVoted
        binding.tvTopCount.text = try {
            "${(topCount.toFloat() / allCount * 100).toInt()}% ($topCount)"
        } catch (e: Exception) {
            "0% ($topCount)"
        }

        binding.vgBottom.alpha = when (state.votedOptionId) {
            state.top.id -> 0.69f
            state.bottom.id -> 1f
            else -> 1f
        }
        binding.tvBottom.text = state.bottom.title
        binding.tvBottomCount.isVisible = isVoted
        binding.tvBottomCount.text = try {
            "${(bottomCount.toFloat() / allCount * 100).toInt()}% ($bottomCount)"
        } catch (e: Exception) {
            "0% ($bottomCount)"
        }

        if (isVoted) {
            if (!binding.tvStatistic.isVisible) {
                binding.tvStatistic.animateShow()
                binding.ivNext.animateShow()
            }
        } else {
            if (binding.tvStatistic.isVisible) {
                binding.tvStatistic.animateHide()
                binding.ivNext.animateHide()
            }
        }
    }


}