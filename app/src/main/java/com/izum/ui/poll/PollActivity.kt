package com.izum.ui.poll

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.izum.R
import com.izum.databinding.ActivityPollBinding
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("ClickableViewAccessibility")
class PollActivity : ComponentActivity() {

    companion object {
        const val KEY_ARGS_PACK_ID = "KEY_ARGS_PACK_ID"

        private val LONG_PRESS_DURATION = 750L
    }

    @Inject lateinit var router: Router

    private var _binding: ActivityPollBinding? = null
    private val binding: ActivityPollBinding
        get() = _binding!!

    private val viewModel: PollViewModel by viewModels()
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPollBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)
        initView()

        router.attachHost(this@PollActivity)

        lifecycleScope.launch {
            viewModel.uiStateFlow.collect { state -> update(state) }
        }

        viewModel.init(
            args = PollViewModel.Companion.Arguments(
                packId = intent.getLongExtra(KEY_ARGS_PACK_ID, -1)
            )
        )

        update(PollViewState.Loading)
    }

    private fun initView() {
        binding.vBack.setOnClickListener {
            finish()
        }
        binding.vNext.setOnClickListener {
            viewModel.onNextClick()
        }
        binding.vTop.setOnTouchListener(
            onVoted = { viewModel.onTopVoted() },
            onInterrupted = { viewModel.onTopInterrupted() }
        )
        binding.vBottom.setOnTouchListener(
            onVoted = { viewModel.onBottomVoted() },
            onInterrupted = { viewModel.onBottomInterrupted() }
        )
    }

    private fun update(state: PollViewState) {
        binding.gLoading.isVisible = state is PollViewState.Loading
        binding.gPoll.isVisible = state is PollViewState.Poll
        binding.gVotedPoll.isVisible = state is PollViewState.VotedPoll
        binding.vAgreeWithYou.isVisible = state is PollViewState.VotedPoll
        binding.vTop.isClickable = state is PollViewState.Poll
        binding.vBottom.isClickable = state is PollViewState.Poll

        when(state) {
            is PollViewState.Poll -> showPoll(state)
            is PollViewState.VotedPoll -> showVotedPoll(state)
            is PollViewState.Loading -> { /* nothing */ }
        }
    }

    private fun showPoll(poll: PollViewState.Poll) {
        binding.vVotes.text = "Votes: ${poll.votesCount}"

        binding.vTop.text = poll.top.title
        binding.vTop.setBackgroundResource(R.drawable.ripple_poll)
        binding.vTop.setTextColor(getColor(R.color.black))

        binding.vBottom.text = poll.bottom.title
        binding.vBottom.setBackgroundResource(R.drawable.ripple_poll)
        binding.vBottom.setTextColor(getColor(R.color.black))
    }

    private fun showVotedPoll(votedPoll: PollViewState.VotedPoll) {
        val poll = votedPoll.poll
        val votedOptionId = votedPoll.votedOptionId

        showPoll(poll)

        val agreeWithYouPercent = (poll.votesCount / 100f) * when(votedOptionId) {
            poll.top.id -> poll.top.votesCount
            poll.bottom.id -> poll.bottom.votesCount
            else -> throw IllegalStateException("Unknown voted option id: $votedOptionId")
        }
        binding.vAgreeWithYou.text = "Agree with you: $agreeWithYouPercent}"

        val votedTextView = when(votedOptionId) {
            poll.top.id -> binding.vTop
            poll.bottom.id -> binding.vBottom
            else -> throw IllegalStateException("Unknown voted option id: $votedOptionId")
        }

        votedTextView.setBackgroundColor(getColor(R.color.colorAccent))
        votedTextView.setTextColor(getColor(R.color.white))
    }

    private fun TextView.setOnTouchListener(
        onVoted: () -> Unit,
        onInterrupted: () -> Unit,
    ) {
        setOnTouchListener { view, motionEvent ->
            val x = motionEvent.x + view.left
            val y = motionEvent.y + view.top

            view.drawableHotspotChanged(x, y)

            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.isPressed = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.isPressed = false
            }

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.postDelayed({
                        onVoted.invoke()
                    }, LONG_PRESS_DURATION)
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacksAndMessages(null)
                    onInterrupted.invoke()
                }
            }

            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
        binding.vTop.setOnTouchListener(null)
        binding.vBottom.setOnTouchListener(null)
    }

}