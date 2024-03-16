package com.pickone.ui.poll

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.pickone.R
import com.pickone.databinding.ActivityPollBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_PACK
import com.pickone.ui.utils.hideSystemBars
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private var pollId: Long = -1

    private var idTime: Long = -1
    private var startTime: Long = 0
    private var resultTime: Long = 0
    private val currentTime: Long
        get() = System.currentTimeMillis()

    override fun initView(args: Bundle) = with(binding) {
        ivBack.setOnClickListener { finish() }
        vgTop.setOnClickListener { viewModel.onTopVote(elapsedTimeMs = stopTimer()) }
        vgBottom.setOnClickListener { viewModel.onBottomVote(elapsedTimeMs = stopTimer()) }
        tvNext.setOnClickListener { viewModel.onNextClick() }
        tvStatistic.setOnClickListener { viewModel.onStatisticClick() }
        tvRetry.setOnClickListener { viewModel.onRetryClick() }
        tvHistory.setOnClickListener { viewModel.onPackHistoryClick() }

        tvStatistic.isEnabled = false
        tvNext.isEnabled = false

        onBackPressedDispatcher.addCallback(this@PollActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        update(PollViewState.Loading)

        if (intent.hasExtra(KEY_ARGS_PACK)) {
            viewModel.onViewInitialized(intent.getParcelableExtra(KEY_ARGS_PACK)!!)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.hideSystemBars()
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(state: PollViewState) = launch {
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
                val isContainsImages = !state.top.imageUrl.isNullOrBlank() || !state.bottom.imageUrl.isNullOrBlank()
                when {
                    pollId == -1L -> {
                        updateContent(state)
                        showContent(showIcon = isContainsImages)
                    }

                    state.pollId == pollId -> {
                        updateContent(state)
                    }

                    state.pollId != pollId -> {
                        hideContent()
                        delay(400)
                        updateContent(state)
                        showContent(showIcon = isContainsImages)
                    }
                }
            }
            else -> {}
        }
    }

    private fun updateContent(state: PollViewState.Content) = with(binding) {
        pollId = state.pollId
        if (state.pollId != idTime) {
            startTimer(state.pollId)
        }

        val isTopVoted = state.votedOptionId == state.top.id
        val isBottomVoted = state.votedOptionId == state.bottom.id
        val isVoted = isTopVoted || isBottomVoted

        tvPackTitle.text = state.packTitle

        vgTop.alpha = if (state.votedOptionId == state.bottom.id) 0.5f else 1f
        tvTop.text = state.top.title
        tvTopCount.text = state.top.votesText
        tvTopCount.alpha = if (isVoted) 1f else 0f
        ivTop.isVisible = !state.top.imageUrl.isNullOrBlank()

        if (state.top.imageUrl.isNullOrBlank() && ivTop.tag != "") {
            ivTop.tag = ""
            Glide.with(this@PollActivity)
                .clear(ivTop)
        }

        if (!state.top.imageUrl.isNullOrBlank() && ivTop.tag != state.top.imageUrl) {
            ivTop.tag = state.top.imageUrl
            Glide.with(this@PollActivity)
                .load(state.top.imageUrl)
                .override(512,512)
                .into(ivTop)
        }

        vgBottom.alpha = if (state.votedOptionId == state.top.id) 0.5f else 1f
        tvBottom.text = state.bottom.title
        tvBottomCount.text = state.bottom.votesText
        tvBottomCount.alpha = if (isVoted) 1f else 0f
        ivBottom.isVisible = !state.bottom.imageUrl.isNullOrBlank()

        if (state.bottom.imageUrl.isNullOrBlank() && ivBottom.tag != "") {
            ivBottom.tag = ""
            Glide.with(this@PollActivity)
                .clear(ivBottom)
        }

        if (!state.bottom.imageUrl.isNullOrBlank() && ivBottom.tag != state.bottom.imageUrl) {
            ivBottom.tag = state.bottom.imageUrl
            Glide.with(this@PollActivity)
                .load(state.bottom.imageUrl)
                .override(512,512)
                .into(ivBottom)
        }

        tvStatistic.isEnabled = isVoted
        tvNext.isEnabled = isVoted

        if (state.isFinishVisible) {
            tvNext.setCompoundDrawables(null, null, null, null)
            tvNext.text = getString(R.string.finish)
        }
    }

    private fun showContent(showIcon: Boolean) = with(binding) {
        tvTop.showWithAlpha()
        if (showIcon) ivTop.showWithAlpha()
        tvBottom.showWithAlpha()
        if (showIcon) ivBottom.showWithAlpha()
    }

    private fun hideContent() = with(binding) {
        tvTop.hideWithAlpha()
        if (ivTop.alpha > 0f) ivTop.hideWithAlpha()
        tvTopCount.hideWithAlpha()

        tvBottom.hideWithAlpha()
        if (ivBottom.alpha > 0f) ivBottom.hideWithAlpha()
        tvBottomCount.hideWithAlpha()
    }

    private fun View.showWithAlpha() {
        animate().alpha(1f).setDuration(400).start()
    }

    private fun View.hideWithAlpha() {
        animate().alpha(0f).setDuration(400).start()
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

    override fun onMovedToBackground() {
        super.onMovedToBackground()
        viewModel.onMovedToBackground()
    }

}