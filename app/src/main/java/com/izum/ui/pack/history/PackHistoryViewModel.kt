package com.izum.ui.pack.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.R
import com.izum.data.Poll
import com.izum.data.repository.PublicPacksRepository
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.StateViewModel
import com.izum.ui.poll.list.PollsItem
import com.izum.ui.poll.statistic.PollStatisticInput
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PackHistoryViewState {

    object Loading : PackHistoryViewState

    object Empty : PackHistoryViewState

    object Error : PackHistoryViewState

    data class Content(
        val polls: List<PollsItem>,
        val isValueInNumbers: Boolean = true
    ) : PackHistoryViewState

}

@HiltViewModel
class PackHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val publicPacksRepository: PublicPacksRepository,
    private val preferenceCache: PreferenceCache
) : StateViewModel<PackHistoryInput, PackHistoryViewState>(
    initialState = PackHistoryViewState.Loading
) {

    private val polls = mutableListOf<Poll>()
    private var isPollFetched = false
    private var isValueInNumbers = true

    private lateinit var input: PackHistoryInput

    override fun onViewInitialized(input: PackHistoryInput) {
        super.onViewInitialized(input)
        this.input = input

        fetchPolls()
    }

    private fun fetchPolls() = viewModelScope.launch {
        try {
            val newPools = publicPacksRepository.getPackVotedPolls(input.packId)
            preferenceCache.putLong("Pack#${input.packId}", newPools.size.toLong())
            isPollFetched = true
            polls.clear()
            polls.addAll(newPools)
            updateView()
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            isPollFetched = false
            updateState { PackHistoryViewState.Error }
        }
    }

    private fun updateView() {
        updateState {
            if (!isPollFetched) return@updateState PackHistoryViewState.Loading
            if (polls.isEmpty()) return@updateState PackHistoryViewState.Empty

            PackHistoryViewState.Content(
                polls
                    .map { poll ->
                        PollsItem.TwoOptionsBar(
                            id = poll.id,
                            leftTop = PollsItem.TwoOptionsBar.Value(
                                text = poll.options[0].title,
                                color = context.getColor(R.color.red)
                            ),
                            leftBottom = PollsItem.TwoOptionsBar.Value(
                                text = if (!isValueInNumbers) {
                                    "${poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())}%"
                                } else {
                                     poll.options[0].votesCount.toString()
                                },
                                color = context.getColor(R.color.red)
                            ),
                            rightTop = PollsItem.TwoOptionsBar.Value(
                                text = poll.options[1].title,
                                color = context.getColor(R.color.blue)
                            ),
                            rightBottom = PollsItem.TwoOptionsBar.Value(
                                text = if (!isValueInNumbers) {
                                    "${poll.options[1].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())}%"
                                } else {
                                    poll.options[1].votesCount.toString()
                                },
                                color = context.getColor(R.color.blue)
                            ),
                            barPercent = poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())
                        )
                    },
                isValueInNumbers = isValueInNumbers
            )
        }
    }

    fun onNoPollsClicked() {
        viewModelScope.launch {
            route(Router.Route.Finish)
            route(Router.Route.Polls(input.packId, input.packTitle))
        }
    }

    fun onFormatClicked() {
        isValueInNumbers = !isValueInNumbers
        updateView()
    }

    fun onPollClick(id: Long) {
        viewModelScope.launch {
            route(Router.Route.Statistic(PollStatisticInput(pollId = id)))
        }
    }


}