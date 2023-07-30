package com.izum.ui.poll

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.Poll
import com.izum.data.PollOption
import com.izum.data.repository.PacksRepository
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.poll.PollViewModel.Companion.Arguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

sealed interface PollViewState {
    object Loading : PollViewState
    data class Poll(
        val votesCount: Long,
        val top: OptionViewState,
        val bottom: OptionViewState,
        val isPrevButtonEnabled: Boolean,
        val isNextButtonEnabled: Boolean,
        val isSliderTracking: Boolean,
        val pollsLastIndex: Long,
        val pollIndex: Int,
        val lastAvailablePollIndex: Long
    ) : PollViewState

    data class VotedPoll(
        val poll: Poll,
        val votedOptionId: Long,
        val agreePercent: Int = 0
    ) : PollViewState
}

data class OptionViewState(
    val id: Long,
    val title: String,
    val votesCount: Long
)

@HiltViewModel
class PollViewModel @Inject constructor(
    private val packsRepository: PacksRepository,
    private val pollsRepository: PollsRepository
) : StateViewModel<Arguments, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {
        data class Arguments(
            val packId: Long
        )
    }

    private val polls = mutableListOf<Poll>()
    private var index = 0

    private val poll: Poll
        get() = polls[index]

    private val optionTop: PollOption
        get() = poll.options[0]

    private val optionBottom: PollOption
        get() = poll.options[1]

    override fun init(args: Arguments) {
        super.init(args)
        val packId = args.packId

        viewModelScope.launch {
            packsRepository.getPackPolls(packId)
                .collect {
                    polls.clear()
                    polls.addAll(it)
                    updateView()
                }
        }
    }

    private fun updateView() {
        updateState {
            val allVotesCount = poll.options.sumOf { it.votesCount }
            val lastAvailablePollIndex = if (polls.any { it.votedOptionId == null }) {
                polls.indexOfLast { it.votedOptionId == null }
            } else {
                polls.lastIndex
            }

            val pollViewState = PollViewState.Poll(
                votesCount = allVotesCount,
                top = OptionViewState(
                    id = optionTop.id,
                    title = optionTop.title,
                    votesCount = optionTop.votesCount
                ),
                bottom = OptionViewState(
                    id = optionBottom.id,
                    title = optionBottom.title,
                    votesCount = optionBottom.votesCount
                ),
                isPrevButtonEnabled = index > 0,
                isNextButtonEnabled = index < polls.lastIndex,
                isSliderTracking = isSliderTracking,
                pollsLastIndex = polls.lastIndex.toLong(),
                pollIndex = index,
                lastAvailablePollIndex = lastAvailablePollIndex.toLong()
            )

            val votedOptionId = this.poll.votedOptionId
            if (votedOptionId == null) {
                pollViewState
            } else {
                val sameVotesCount = poll.options
                    .firstOrNull() { it.id == votedOptionId }?.votesCount
                    ?: return@updateState pollViewState

                val agreePercent = ((sameVotesCount.toFloat() / allVotesCount.toFloat()) * 100F).toInt()

                PollViewState.VotedPoll(
                    poll = pollViewState.copy(
                        isNextButtonEnabled = index < polls.lastIndex
                    ),
                    votedOptionId = votedOptionId,
                    agreePercent = agreePercent
                )
            }
        }
    }

    fun onNextClick() {
        index = ++index
        updateView()
    }

    fun onPrevClick() {
        index = --index
        updateView()
    }

    fun onTopVoted() = onVoted(optionTop.id)

    fun onBottomVoted() = onVoted(optionBottom.id)

    private fun onVoted(optionId: Long) {
        val viewState = viewState
        if (viewState !is PollViewState.Poll) {
            Log.d("Steve", "onVoted: viewState is not PollViewState.Poll")
            return
        }

        val allVotesCount = poll.options.sumOf { it.votesCount }
        val sameVotesCount = poll.options.first { it.id == optionId }.votesCount
        val agreePercent = ((sameVotesCount.toFloat() / allVotesCount.toFloat()) * 100F).toInt()
        updateState {
            PollViewState.VotedPoll(
                poll = viewState,
                votedOptionId = optionId,
                agreePercent = agreePercent
            )
        }

        viewModelScope.launch {
            try {
                pollsRepository.vote(poll.id, optionId)
            } catch (exception: Exception) {
                emit(ViewAction.ShowToast("Send vote error: poll id: ${poll.id}, option id: ${optionId}"))
                updateState { viewState }
                return@launch
            }

            polls.replaceAll {
                if (it.id == poll.id) {
                    it.copy(votedOptionId = optionId)
                } else {
                    it
                }
            }

            updateView()
        }
    }

    fun onTopInterrupted() {
        // ..
    }

    fun onBottomInterrupted() {
        // ..
    }

    private var isSliderTracking = false

    fun onSliderTrackingStart() {
        isSliderTracking = true
        updateView()
    }

    fun onSliderTrackingStop() {
        isSliderTracking = false
        updateView()
    }

    fun onSliderChanged(index: Int) {
        this.index = index
        updateView()
    }

}