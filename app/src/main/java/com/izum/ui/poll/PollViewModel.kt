package com.izum.ui.poll

import androidx.lifecycle.viewModelScope
import com.izum.data.repository.PacksRepository
import com.izum.data.Poll
import com.izum.domain.core.StateViewModel
import com.izum.ui.poll.PollViewModel.Companion.Arguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PollViewState {
    object Loading : PollViewState
    data class Poll(
        val votesCount: Long,
        val top: OptionViewState,
        val bottom: OptionViewState
    ) : PollViewState

    data class VotedPoll(
        val poll: Poll,
        val votedOptionId: Long
    ) : PollViewState
}

data class OptionViewState(
    val id: Long,
    val title: String,
    val votesCount: Long
)

@HiltViewModel
class PollViewModel @Inject constructor(
    private val packsRepository: PacksRepository
) : StateViewModel<Arguments, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {
        data class Arguments(
            val packId: Long
        )
    }

    private val packPolls = mutableListOf<Poll>()
    private var index = 0

    private val poll: Poll
        get() = packPolls[index]

    override fun init(args: Arguments) {
        super.init(args)
        val packId = args.packId

        viewModelScope.launch {
            packsRepository.getPackPolls(packId)
                .collect { polls ->
                    packPolls.clear()
                    packPolls.addAll(polls)
                    updateView()
                }
        }
    }

    private fun updateView() {
        updateState {
            val optionTop = poll.options[0]
            val optionBottom = poll.options[1]
            val votedOptionId = poll.votedOptionId

            val pollViewState = PollViewState.Poll(
                votesCount = poll.options.sumOf { it.votesCount },
                top = OptionViewState(
                    id = optionTop.id,
                    title = optionTop.title,
                    votesCount = optionTop.votesCount
                ),
                bottom = OptionViewState(
                    id = optionBottom.id,
                    title = optionBottom.title,
                    votesCount = optionBottom.votesCount
                )
            )

            if (votedOptionId == null) {
                pollViewState
            } else {
                PollViewState.VotedPoll(
                    poll = pollViewState,
                    votedOptionId = votedOptionId
                )
            }
        }
    }

    fun onNextClick() {
        // ..
    }

    fun onTopVoted() {
        // ..
    }

    fun onTopInterrupted() {
        // ..
    }

    fun onBottomVoted() {
        // ..
    }

    fun onBottomInterrupted() {
        // ..
    }

}