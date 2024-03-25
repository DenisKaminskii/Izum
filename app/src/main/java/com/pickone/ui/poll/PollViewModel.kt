package com.pickone.ui.poll

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.pickone.analytics.Analytics
import com.pickone.data.Pack
import com.pickone.data.Poll
import com.pickone.data.PollOption
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.domain.core.StateViewModel
import com.pickone.network.NetworkMonitor
import com.pickone.ui.ViewAction
import com.pickone.ui.poll.statistic.PollStatisticInput
import com.pickone.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

sealed interface PollViewState {

    object Loading : PollViewState

    data class Empty(
        val packTitle: String
    ) : PollViewState

    object Error : PollViewState

    data class Content(
        val pollId: Long,
        val packTitle: String,
        val top: OptionViewState,
        val bottom: OptionViewState,
        val votedOptionId: Long? = null,
        val isFinishVisible: Boolean = false
    ) : PollViewState

}

data class OptionViewState(
    val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val votesText: String
)

@HiltViewModel
class PollViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val publicPacksRepository: PublicPacksRepository,
    private val customPacksRepository: CustomPacksRepository,
    private val preferenceCache: PreferenceCache,
    private val analytics: Analytics,
    private val networkMonitor: NetworkMonitor
) : StateViewModel<Pack, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {
        private const val VotesUntilGoogleReview = 15
        private const val PreloadPollsCount = 7
    }

    private lateinit var pack: Pack

    private val polls = mutableListOf<Poll>()
    private var isPollFetched = false

    private val poll: Poll
        get() = polls.first()

    private val optionTop: PollOption
        get() = poll.options[0]

    private val optionBottom: PollOption
        get() = poll.options[1]

    private var votedOptionId: Long? = null

    private var preloadImagesCounter = 0

    override fun onViewInitialized(input: Pack) {
        super.onViewInitialized(input)
        this.pack = input
        fetchPolls()

        viewModelScope.launch {
            networkMonitor.isOnline
                .collect { isOnline ->
                    if (isOnline && !isPollFetched) {
                        fetchPolls()
                    }
                }
        }
    }

    private val fetchMutex = Mutex()
    private fun fetchPolls() = viewModelScope.launch(IO) {
        if (fetchMutex.isLocked) return@launch
        fetchMutex.withLock {
            try {
                val newPolls = if (pack is Pack.Custom) {
                    customPacksRepository.getPackUnvotedPolls(pack.id)
                } else {
                    publicPacksRepository.getPackUnvotedPolls(pack.id)
                }
                isPollFetched = true
                polls.clear()
                polls.addAll(newPolls)
                updateView()
                preloadNextImages()
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to fetch polls")
                isPollFetched = false
                updateState { PollViewState.Error }
            }
        }
    }

    private fun preloadNextImages() = viewModelScope.launch(IO) {
        polls.take(PreloadPollsCount).forEach {
            launch { preloadImages(it) }.join()
        }
    }

    private fun preloadImages(poll: Poll) {
        if (poll.options.none { !it.imageUrl.isNullOrBlank() }) return

        poll.options.forEach {
            Glide.with(context).load(it.imageUrl).override(512, 512).preload()
        }
    }

    private fun updateView() {
        updateState {
            if (!isPollFetched) return@updateState PollViewState.Loading
            if (polls.isEmpty()) return@updateState PollViewState.Empty(
                packTitle = pack.title
            )

            val topCount = optionTop.votesCount + if (votedOptionId == optionTop.id) 1 else 0
            val bottomCount = optionBottom.votesCount + if (votedOptionId == optionBottom.id) 1 else 0
            val allCount = topCount + bottomCount

            PollViewState.Content(
                pollId = poll.id,
                packTitle = pack.title,
                top = OptionViewState(
                    id = optionTop.id,
                    title = optionTop.title,
                    votesText = try {
                        "${(topCount.toFloat() / allCount * 100).toInt()}% ($topCount)"
                    } catch (e: Exception) {
                        "0% ($topCount)"
                    },
                    imageUrl = optionTop.imageUrl
                ),
                bottom = OptionViewState(
                    id = optionBottom.id,
                    title = optionBottom.title,
                    votesText = try {
                        "${(bottomCount.toFloat() / allCount * 100).toInt()}% ($bottomCount)"
                    } catch (e: Exception) {
                        "0% ($bottomCount)"
                    },
                    imageUrl = optionBottom.imageUrl
                ),
                votedOptionId = votedOptionId,
                isFinishVisible = polls.size < 2
            )
        }
    }

    fun onNextClick() = doubleClickProtection {
        votedOptionId = null
        polls.removeFirstOrNull()
        updateView()

        checkGoogleStoreReviewShow()

        if (++preloadImagesCounter >= (PreloadPollsCount - 2)) {
            preloadImagesCounter = 0
            preloadNextImages()
        }
    }

    private fun checkGoogleStoreReviewShow() {
        val isShown = preferenceCache.getBoolean(PreferenceKey.GoogleStoreReviewShown.name, false)
        val commonVotesCount = preferenceCache.getLong(PreferenceKey.CommonVotesCount.name, 0L)
        if (!isShown && commonVotesCount >= VotesUntilGoogleReview) {
            val manager = ReviewManagerFactory.create(context)

            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        route(Router.Route.GoogleReview(reviewInfo = task.result))
                    }
                } else {
                    @ReviewErrorCode val reviewErrorCode = (task.getException() as ReviewException).errorCode
                    Timber.e(task.exception,"Review error: $reviewErrorCode")
                }
            }
        }
    }

    fun onStatisticClick() = doubleClickProtection {
        votedOptionId?.let {
            viewModelScope.launch {
                route(
                    Router.Route.Statistic(
                        PollStatisticInput(poll.id, isCustomPack = pack is Pack.Custom, votedOptionId = it)
                    )
                )
            }
        } ?: viewModelScope.launch {
            emit(ViewAction.ShowToast("You should vote first"))
        }
    }

    fun onRetryClick() = doubleClickProtection {
        fetchPolls()
        updateView()
    }

    fun onPackHistoryClick() = doubleClickProtection {
        viewModelScope.launch {
            route(Router.Route.Finish)
            route(Router.Route.PackHistory(pack))
        }
    }

    fun onMovedToBackground() {
        analytics.pollExited(poll.id)
    }

    fun onTopVote(elapsedTimeMs: Long) = doubleClickProtection {
        onVote(optionTop.id, elapsedTimeMs)
    }

    fun onBottomVote(elapsedTimeMs: Long) = doubleClickProtection {
        onVote(optionBottom.id, elapsedTimeMs)
    }

    private fun onVote(optionId: Long, elapsedTimeMs: Long) {
        if (votedOptionId != null) return
        votedOptionId = optionId
        updateView()

        viewModelScope.launch {
            try {
                if (pack is Pack.Custom) {
                    customPacksRepository.vote(poll.id, optionId, elapsedTimeMs.toInt())
                    analytics.customPollVoted(poll.id, optionId)
                } else {
                    publicPacksRepository.vote(poll.id, optionId, elapsedTimeMs.toInt())
                    analytics.pollVoted(poll.id, optionId)
                }

                increaseAnsweredPollsCount()
            } catch (exception: Exception) {
                Timber.e(exception, "Failed while voting")
                emit(ViewAction.ShowToast("No internet connection \uD83D\uDCE1"))
                updateState { viewState }
                return@launch
            }
        }
    }

    private fun increaseAnsweredPollsCount() {
        val keyPack = "${pack.id}_voted_count"
        val packVotesCount = preferenceCache.getLong(keyPack, 0L)
        preferenceCache.putLong(keyPack, packVotesCount + 1)

        val commonVotesCount = preferenceCache.getLong(PreferenceKey.CommonVotesCount.name, 0L)
        preferenceCache.putLong(PreferenceKey.CommonVotesCount.name, commonVotesCount + 1)
    }

    private val clickMutex = Mutex()
    private fun doubleClickProtection(block: () -> Unit) = viewModelScope.launch {
        if (clickMutex.isLocked) return@launch

        clickMutex.withLock {
            block.invoke()
            delay(750)
        }
    }

}