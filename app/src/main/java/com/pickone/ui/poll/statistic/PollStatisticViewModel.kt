package com.pickone.ui.poll.statistic

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.pickone.R
import com.pickone.analytics.Analytics
import com.pickone.data.PollStatistic
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.data.repository.UserRepository
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.ViewAction
import com.pickone.ui.poll.list.PollsItem
import com.pickone.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface PollStatisticViewState {

    object Loading : PollStatisticViewState

    object Error : PollStatisticViewState

    data class NoSubscription(
        val options: PollsItem.TwoOptionsBar
    ) : PollStatisticViewState

    data class NoData(
        val options: PollsItem.TwoOptionsBar,
        val isShowSharePack: Boolean = false
    ) : PollStatisticViewState

    data class Stats(
        val stats: List<PollsItem>,
        val isValueInNumbers: Boolean = true
    ) : PollStatisticViewState
}

@HiltViewModel
class PollStatisticViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val customPacksRepository: CustomPacksRepository,
    private val publicPacksRepository: PublicPacksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userRepository: UserRepository,
    private val analytics: Analytics
) : StateViewModel<PollStatisticInput, PollStatisticViewState>(
    initialState = PollStatisticViewState.Loading
) {

    private var statistic: PollStatistic? = null
    private var pollId = -1L
    private var shareLink: String? = null
    private var isCustomPack = false
    private var votedOptionId: Long? = null
    private var isValueInNumbers = true

    override fun onViewInitialized(input: PollStatisticInput) {
        super.onViewInitialized(input)
        pollId = input.pollId
        shareLink = input.shareLink
        isCustomPack = input.isCustomPack
        votedOptionId = input.votedOptionId

        viewModelScope.launch {
            fetchStatistic()
        }
    }

    private suspend fun fetchStatistic() = withContext(ioDispatcher) {
        try {
            updateView()
            statistic = if (isCustomPack) {
                customPacksRepository.getPollStatistic(pollId)
            } else {
                publicPacksRepository.getPollStatistic(pollId)
            }
            updateView()
        } catch (ex: Exception) {
            updateState { PollStatisticViewState.Error }
        }
    }

    private fun updateView() = viewModelScope.launch {
        statistic
            ?.let { pollStatistic ->
                val leftVotesCount = pollStatistic.options[0].votesCount
                val rightCount = pollStatistic.options[1].votesCount

                val leftFakeVote = if (votedOptionId == pollStatistic.options[0].id) 1 else 0
                val rightFakeVote = if (votedOptionId == pollStatistic.options[1].id) 1 else 0

                val sumCount = (leftVotesCount + rightCount).toFloat() + leftFakeVote + rightFakeVote

                val leftPercent = if (sumCount == 0f) 50 else (leftVotesCount * 100 / sumCount).toInt()
                val rightPercent = if (sumCount == 0f) 50 else (rightCount * 100 / sumCount).toInt()

                val optionsItem = PollsItem.TwoOptionsBar(
                    id = pollId,
                    leftTop = PollsItem.TwoOptionsBar.Value(
                        text = if (!isValueInNumbers) {
                            "$leftPercent%"
                        } else {
                            leftVotesCount.toString()
                        },
                        color = context.getColor(R.color.red)
                    ),
                    rightTop = PollsItem.TwoOptionsBar.Value(
                        text = if (!isValueInNumbers) {
                            "$rightPercent%"
                        } else {
                            rightCount.toString()
                        },
                        color = context.getColor(R.color.blue)
                    ),
                    leftBottom = PollsItem.TwoOptionsBar.Value(
                        text = pollStatistic.options[0].title,
                        color = context.getColor(R.color.red)
                    ),
                    rightBottom = PollsItem.TwoOptionsBar.Value(
                        text = pollStatistic.options[1].title,
                        color = context.getColor(R.color.blue)
                    ),
                    barPercent = leftPercent
                )

                if (userRepository.hasSubscription) {
                    if (sumCount == 0f) {
                        updateState {
                            PollStatisticViewState.NoData(
                                options = optionsItem
                            )
                        }
                    } else {
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
                                    id = pollId,
                                    leftTop = PollsItem.TwoOptionsBar.Value(
                                        text = category.title,
                                        color = context.getColor(R.color.white)
                                    ),
                                    rightTop = null,
                                    leftBottom = PollsItem.TwoOptionsBar.Value(
                                        text = if (!isValueInNumbers) {
                                            "$categoryLeftPercent%"
                                        } else {
                                            categoryLeftCount.toInt().toString()
                                        },
                                        color = context.getColor(R.color.red)
                                    ),
                                    rightBottom = PollsItem.TwoOptionsBar.Value(
                                        text = if (!isValueInNumbers) {
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

                        updateState {
                            PollStatisticViewState.Stats(
                                stats = listOf(optionsItem) + statsItems,
                                isValueInNumbers = isValueInNumbers
                            )
                        }
                    }
                } else {
                    updateState {
                        PollStatisticViewState.NoSubscription(
                            options = optionsItem
                        )
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

    fun onFormatClicked() {
        isValueInNumbers = !isValueInNumbers
        updateView()
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            route(Router.Route.Paywall())
        }
    }

    fun onShareClick(clipBoard: ClipboardManager) {
        analytics.customPackShareTap()

        if (shareLink == null) {
            viewModelScope.launch { emit(ViewAction.ShowToast("Something get wrong. Try again.")) }
            return
        }

        viewModelScope.launch {
            val clip: ClipData = ClipData.newPlainText("Polleo Pack Share", shareLink)
            clipBoard.setPrimaryClip(clip)
            emit(ViewAction.ShowToast("Link copied to clipboard!"))
        }
    }

}