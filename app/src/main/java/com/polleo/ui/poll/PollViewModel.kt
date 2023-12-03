package com.polleo.ui.poll

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import com.polleo.data.Poll
import com.polleo.data.PollOption
import com.polleo.data.repository.PublicPacksRepository
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceKey
import com.polleo.domain.core.StateViewModel
import com.polleo.ui.ViewAction
import com.polleo.ui.pack.history.PackHistoryInput
import com.polleo.ui.poll.PollViewModel.Companion.Arguments
import com.polleo.ui.poll.statistic.PollStatisticInput
import com.polleo.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PollViewState {

    object Loading : PollViewState

    data class Empty(
        val packTitle: String
    ) : PollViewState

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
    @ApplicationContext private val context: Context,
    private val publicPacksRepository: PublicPacksRepository,
    private val preferenceCache: PreferenceCache
) : StateViewModel<Arguments, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {

        private const val VOTES_UNTIL_GOOGLE_STORE_REVIEW = 1 // 80

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

    override fun onViewInitialized(input: Arguments) {
        super.onViewInitialized(input)
        packId = input.packId
        packTitle = input.packTitle

        fetchPolls()
    }

    private fun fetchPolls() = viewModelScope.launch {
        try {
            val newPolls = publicPacksRepository.getPackUnvotedPolls(packId)
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
            if (polls.isEmpty()) return@updateState PollViewState.Empty(
                packTitle = packTitle
            )

            val topCount = optionTop.votesCount + if (votedOptionId == optionTop.id) 1 else 0
            val bottomCount = optionBottom.votesCount + if (votedOptionId == optionBottom.id) 1 else 0
            val allCount = topCount + bottomCount

            PollViewState.Content(
                packTitle = packTitle,
                votesCount = allCount,
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
        polls.removeFirstOrNull()
        votedOptionId = null
        updateView()
        checkGoogleStoreReviewShow()
    }

    fun onTopVote() {
        onVote(optionTop.id)
    }

    fun onBottomVote() {
        onVote(optionBottom.id)
    }

    private fun checkGoogleStoreReviewShow() {
        val isShown = preferenceCache.getBoolean(PreferenceKey.GoogleStoreReviewShown, false)
        val commonVotesCount = preferenceCache.getLong(PreferenceKey.CommonVotesCount.name, 0L)
        if (!isShown && commonVotesCount >= VOTES_UNTIL_GOOGLE_STORE_REVIEW) {
            val manager = ReviewManagerFactory.create(context)

            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        route(Router.Route.GoogleReview(reviewInfo = task.result))
                    }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.getException() as ReviewException).errorCode
                    Log.d("Steve", "Review error: $reviewErrorCode")
                }
            }
        }
    }

    fun onStatisticClick() {
        votedOptionId?.let {
            viewModelScope.launch {
                route(
                    Router.Route.Statistic(
                        PollStatisticInput(poll.id, isCustomPack = false, votedOptionId = it)
                    )
                )
            }
        } ?: viewModelScope.launch {
            emit(ViewAction.ShowToast("You should vote first"))
        }
    }

    fun onRetryClick() {
        fetchPolls()
        updateView()
    }

    fun onPackHistoryClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
            route(
                Router.Route.PackHistory(
                    input = PackHistoryInput(
                        packId = packId,
                        packTitle = packTitle
                    )
                )
            )
        }
    }

    private fun onVote(optionId: Long) {
        if (votedOptionId != null) return
        votedOptionId = optionId
        updateView()

        viewModelScope.launch {
            try {
                publicPacksRepository.vote(poll.id, optionId)
                increaseAnsweredPollsCount()
            } catch (exception: Exception) {
                emit(ViewAction.ShowToast("Send vote error: poll id: ${poll.id}, option id: ${optionId}"))
                updateState { viewState }
                return@launch
            }
        }
    }

    private fun increaseAnsweredPollsCount() {
        val keyPack = "${packId}_voted_count"
        val packVotesCount = preferenceCache.getLong(keyPack, 0L)
        preferenceCache.putLong(keyPack, packVotesCount + 1)

        val commonVotesCount = preferenceCache.getLong(PreferenceKey.CommonVotesCount.name, 0L)
        preferenceCache.putLong(PreferenceKey.CommonVotesCount.name, commonVotesCount + 1)
    }

}