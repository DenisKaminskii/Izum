package com.izum.ui.poll.statistic

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.izum.R
import com.izum.data.PollStatistic
import com.izum.data.repository.PollsRepository
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.poll.list.PollsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PollStatisticViewState {

    object Loading : PollStatisticViewState

    object Error : PollStatisticViewState

    data class NoData(
        val options: PollsItem.TwoOptionsBar
    ) : PollStatisticViewState

    data class Stats(
        val stats: List<PollsItem>
    ) : PollStatisticViewState
}

@HiltViewModel
class PollStatisticViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val pollsRepository: PollsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<Long, PollStatisticViewState>(
    initialState = PollStatisticViewState.Loading
) {

    private var statistic: PollStatistic? = null
    private var pollId = -1L
    private var isValueInPercent = true

    override fun onViewInitialized(input: Long) {
        super.onViewInitialized(input)
        pollId = input

        viewModelScope.launch {
            fetchStatistic()
        }
    }

    private suspend fun fetchStatistic() = withContext(ioDispatcher) {
        try {
            updateView()
            statistic = pollsRepository.getPollStatistic(pollId)
            updateView()
        } catch (ex: Exception) {
            updateState { PollStatisticViewState.Error }
        }
    }

    private fun updateView() = viewModelScope.launch {
        statistic
            ?.let { pollStatistic ->
                val leftCount = pollStatistic.options[0].votesCount
                val rightCount = pollStatistic.options[1].votesCount
                val sumCount = (leftCount + rightCount).toFloat()

                val leftPercent = try { leftCount * 100 / sumCount } catch (ex: Exception) { 0 }
                val rightPercent = try { rightCount * 100 / sumCount } catch (ex: Exception) { 0 }

                if (sumCount == 0f) {
                    val optionsItem = PollsItem.TwoOptionsBar(
                        leftBottom = PollsItem.TwoOptionsBar.Value(
                            text = pollStatistic.options[0].title,
                            color = context.getColor(R.color.red)
                        ),
                        rightBottom = PollsItem.TwoOptionsBar.Value(
                            text = pollStatistic.options[1].title,
                            color = context.getColor(R.color.blue)
                        ),
                        barPercent = 50
                    )

                    updateState {
                        PollStatisticViewState.NoData(optionsItem)
                    }
                } else {
                    updateState { currentViewState ->
                        val optionsItem = PollsItem.TwoOptionsBar(
                            leftBottom = PollsItem.TwoOptionsBar.Value(
                                text = pollStatistic.options[0].title,
                                color = context.getColor(R.color.red)
                            ),
                            rightBottom = PollsItem.TwoOptionsBar.Value(
                                text = pollStatistic.options[1].title,
                                color = context.getColor(R.color.blue)
                            ),
                            barPercent = leftPercent.toInt()
                        )

                        val statsItems = pollStatistic.sections.flatMap { section ->
                            listOf(
                                PollsItem.Header(section.title)
                            ) + section.categories.map { category ->
                                val categoryLeftCount = category.options[0].votesCount.toFloat()
                                val categoryRightCount = category.options[1].votesCount.toFloat()
                                val categorySumCount = categoryLeftCount + categoryRightCount

                                val categoryLeftPercent: Int = try {
                                    (categoryLeftCount * 100 / categorySumCount).toInt()
                                } catch (ex: Exception) { 0 }

                                val categoryRightPercent: Int = try {
                                    (categoryRightCount * 100 / categorySumCount).toInt()
                                } catch (ex: Exception) { 0 }

                                PollsItem.TwoOptionsBar(
                                    leftTop = PollsItem.TwoOptionsBar.Value(
                                        text = category.title,
                                        color = context.getColor(R.color.white)
                                    ),
                                    rightTop = null,
                                    leftBottom = PollsItem.TwoOptionsBar.Value(
                                        text = if (isValueInPercent) {
                                            "$categoryLeftPercent%"
                                        } else {
                                            categoryLeftCount.toInt().toString()
                                        },
                                        color = context.getColor(R.color.red)
                                    ),
                                    rightBottom = PollsItem.TwoOptionsBar.Value(
                                        text = if (isValueInPercent) {
                                            "$categoryRightPercent%"
                                        } else {
                                            categoryRightCount.toInt().toString()
                                        },
                                        color = context.getColor(R.color.blue)
                                    ),
                                    barPercent = categoryLeftPercent
                                )
                            }
                        }


                        PollStatisticViewState.Stats(listOf(optionsItem) + statsItems)
                    }
                }
            }
            ?: updateState { PollStatisticViewState.Loading }
    }

    fun onRetryClick() {
        viewModelScope.launch {
            fetchStatistic()
        }
    }

    fun onStatisticClick() {
        isValueInPercent = !isValueInPercent
        updateView()
    }

    fun onShareClick() {
        // §
    }

}