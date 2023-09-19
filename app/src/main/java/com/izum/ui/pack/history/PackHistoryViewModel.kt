package com.izum.ui.pack.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.R
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.data.repository.PublicPacksRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.poll.list.PollsItem
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
        val polls: List<PollsItem>
    ) : PackHistoryViewState

}

@HiltViewModel
class PackHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val publicPacksRepository: PublicPacksRepository,
) : StateViewModel<PackHistoryViewModel.Args, PackHistoryViewState>(
    initialState = PackHistoryViewState.Loading
) {

    data class Args(
        val publicPack: Pack.Public
    )

    private lateinit var publicPack: Pack.Public

    private val polls = mutableListOf<Poll>()
    private var isPollFetched = false
    private var isValueInPercent = true

    override fun onViewInitialized(input: Args) {
        super.onViewInitialized(input)
        publicPack = input.publicPack

        fetchPolls()
    }

    private fun fetchPolls() = viewModelScope.launch {
        try {
            val newPools = publicPacksRepository.getPackVotedPolls(publicPack.id)
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
                            leftTop = PollsItem.TwoOptionsBar.Value(
                                text = poll.options[0].title,
                                color = context.getColor(R.color.red)
                            ),
                            leftBottom = PollsItem.TwoOptionsBar.Value(
                                text = if (isValueInPercent) {
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
                                text = if (isValueInPercent) {
                                    "${poll.options[1].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())}%"
                                } else {
                                    poll.options[1].votesCount.toString()
                                },
                                color = context.getColor(R.color.blue)
                            ),
                            barPercent = poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())
                        )
                    }
            )
        }
    }

    fun onNoPollsClicked() {
        viewModelScope.launch {
            route(Router.Route.Finish)
            route(Router.Route.Polls(publicPack))
        }
    }

    fun onPollClick() {
        isValueInPercent = !isValueInPercent
        updateView()
    }

}