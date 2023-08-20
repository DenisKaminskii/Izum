package com.izum.ui.poll

import androidx.lifecycle.viewModelScope
import com.izum.data.Poll
import com.izum.data.PollOption
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.poll.PollViewModel.Companion.Arguments
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PollViewState {

    object Loading : PollViewState

    data class Poll(
        val packTitle: String,
        val votesCount: Long,
        val top: OptionViewState,
        val bottom: OptionViewState,
        val votedOptionId: Long? = null,
        val isFinishVisible: Boolean = false
    ) : PollViewState

}

data class OptionViewState(
    val id: Long,
    val title: String,
    val votesCount: Long,
    val votesText: String
)

@HiltViewModel
class PollViewModel @Inject constructor(
    private val pollsRepository: PollsRepository
) : StateViewModel<Arguments, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {
        data class Arguments(
            val packId: Long,
            val packTitle: String
        )
    }

    private var packTitle = ""
    private val polls = mutableListOf<Poll>()

    private val poll: Poll
        get() = polls.first()

    private val optionTop: PollOption
        get() = poll.options[0]

    private val optionBottom: PollOption
        get() = poll.options[1]

    private var votedOptionId: Long? = null
    private var jobVoting: Job? = null

    override fun onViewInitialized(args: Arguments) {
        super.onViewInitialized(args)
        val packId = args.packId
        packTitle = args.packTitle

        fetchPolls(packId)
    }

    private fun fetchPolls(packId: Long) = viewModelScope.launch {
        val newPolls = pollsRepository.getPackPolls(packId)
        polls.clear()
        polls.addAll(newPolls)
        updateView()
    }

    private fun updateView() {
        updateState {
            val topCount = optionTop.votesCount
            val bottomCount = optionBottom.votesCount
            val allCount = topCount + bottomCount

            PollViewState.Poll(
                packTitle = packTitle,
                votesCount = poll.options.sumOf { option -> option.votesCount },
                top = OptionViewState(
                    id = optionTop.id,
                    title = optionTop.title,
                    votesCount = topCount.let {
                        if (votedOptionId == optionTop.id) it + 1 else it
                    },
                    votesText = try {
                        "${(topCount.toFloat() / allCount * 100).toInt()}% ($topCount)"
                    } catch (e: Exception) {
                        "0% ($topCount)"
                    }
                ),
                bottom = OptionViewState(
                    id = optionBottom.id,
                    title = optionBottom.title,
                    votesCount = optionBottom.votesCount.let {
                        if (votedOptionId == optionBottom.id) it + 1 else it
                    },
                    votesText = try {
                        "${(bottomCount.toFloat() / allCount * 100).toInt()}% ($bottomCount)"
                    } catch (e: Exception) {
                        "0% ($bottomCount)"
                    }
                ),
                votedOptionId = votedOptionId,
                isFinishVisible = polls.size < 2
            )
        }
    }

    fun onNextClick() {
        if (jobVoting?.isActive == true) {
            viewModelScope.launch {
                emit(ViewAction.ShowToast("Uploading vote..."))
            }
            return
        }

        if (polls.size < 2) {
            viewModelScope.launch {
                route(Router.Route.Finish)
            }

        } else {
            polls.removeFirstOrNull()
            votedOptionId = null
            updateView()
        }
    }

    fun onTopVote() {
        onVote(optionTop.id)
    }

    fun onBottomVote() {
        onVote(optionBottom.id)
    }

    fun onStatisticClick() {
        viewModelScope.launch {
            route(Router.Route.Statistic(poll))
        }
    }

    private fun onVote(optionId: Long) {
        if (votedOptionId != null) return
        votedOptionId = optionId
        updateView()

        jobVoting = viewModelScope.launch {
            try {
                pollsRepository.vote(poll.id, optionId)
            } catch (exception: Exception) {
                emit(ViewAction.ShowToast("Send vote error: poll id: ${poll.id}, option id: ${optionId}"))
                updateState { viewState }
                return@launch
            }
        }
    }

}