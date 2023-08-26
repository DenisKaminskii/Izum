package com.izum.ui.poll

import android.util.Log
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

    object Empty : PollViewState

    object Error : PollViewState

    data class Content(
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
    private var packId = -1L

    private val polls = mutableListOf<Poll>()
    private var isPollFetched = false

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
        packId = args.packId
        packTitle = args.packTitle

        fetchPolls()
    }

    private fun fetchPolls() = viewModelScope.launch {
        try {
            val newPolls = pollsRepository.getPackUnvotedPolls(packId)
            isPollFetched = true
            polls.clear()
            polls.addAll(newPolls)
            updateView()
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            isPollFetched = false
            updateState { PollViewState.Error }
        }
    }

    private fun updateView() {
        updateState {
            if (!isPollFetched) return@updateState PollViewState.Loading
            if (polls.isEmpty()) return@updateState PollViewState.Empty

            val topCount = optionTop.votesCount
            val bottomCount = optionBottom.votesCount
            val allCount = topCount + bottomCount

            PollViewState.Content(
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

        polls.removeFirstOrNull()
        votedOptionId = null
        updateView()
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

    fun onRetryClick() {
        fetchPolls()
        updateView()
    }

    fun onSuggestClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
            route(Router.Route.SuggestPoll)
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