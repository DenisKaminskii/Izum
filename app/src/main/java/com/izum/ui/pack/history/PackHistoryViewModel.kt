package com.izum.ui.pack.history

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.izum.R
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.pack.history.PackHistoryViewModel.Companion.Arguments
import com.izum.ui.poll.statistic.StatisticItem
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PackHistoryViewState {

    object Loading : PackHistoryViewState

    data class Polls(
        val polls: List<StatisticItem>
    ) : PackHistoryViewState

}

@HiltViewModel
class PackHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pollsRepository: PollsRepository,

) : StateViewModel<Arguments, PackHistoryViewState>(
    initialState = PackHistoryViewState.Loading
) {

    companion object {
        data class Arguments(
            val pack: Pack
        )
    }

    private val polls = mutableListOf<Poll>()
    private lateinit var pack: Pack
    private var isValueInPercent = true

    override fun onViewInitialized(args: Arguments) {
        super.onViewInitialized(args)
        pack = args.pack

        viewModelScope.launch {
            pollsRepository.getPackVotedPolls(args.pack.id)
                .collect { polls ->
                    this@PackHistoryViewModel.polls.clear()
                    this@PackHistoryViewModel.polls.addAll(polls)
                    updateView()
                }
        }
    }

    private fun updateView() {
        updateState {
            PackHistoryViewState.Polls(
                polls
                    .map { poll ->
                        StatisticItem.TwoOptionsBar(
                            leftTop = StatisticItem.TwoOptionsBar.Value(
                                text = poll.options[0].title,
                                color = context.getColor(R.color.red)
                            ),
                            leftBottom = StatisticItem.TwoOptionsBar.Value(
                                text = if (isValueInPercent) {
                                    "${poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())}%"
                                } else {
                                    poll.options[0].votesCount.toString()
                                },
                                color = context.getColor(R.color.red)
                            ),
                            rightTop = StatisticItem.TwoOptionsBar.Value(
                                text = poll.options[1].title,
                                color = context.getColor(R.color.blue)
                            ),
                            rightBottom = StatisticItem.TwoOptionsBar.Value(
                                text = if (isValueInPercent) {
                                    "${poll.options[1].votesCount.toInt() * 100 / (poll.options[1].votesCount.toInt() + poll.options[0].votesCount.toInt())}%"
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
            route(Router.Route.Polls(pack))
        }
    }

    fun onPollClick() {
        isValueInPercent = !isValueInPercent
        updateView()
    }

}