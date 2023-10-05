package com.polleo.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.polleo.data.Poll
import com.polleo.data.repository.CustomPacksRepository
import com.polleo.data.repository.UserRepository
import com.polleo.di.IoDispatcher
import com.polleo.domain.core.StateViewModel
import com.polleo.ui.ViewAction
import com.polleo.ui.create.EditPackViewModel.Companion.POLLS_MAXIMUM_UNSUBSCRIBED
import com.polleo.ui.edit.EditPollVariant
import com.polleo.ui.poll.list.PollsItem
import com.polleo.ui.poll.statistic.PollStatisticInput
import com.polleo.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EditPackViewState {

    object Loading : EditPackViewState

    object Error : EditPackViewState

    data class Content(
        val title: String = "",
        val polls: List<PollsItem> = emptyList(),
        val isAddButtonVisible: Boolean = true,
        val isShareButtonEnabled: Boolean = false,
        val isEditButtonEnabled: Boolean = false,
        val pollsMax: Int = POLLS_MAXIMUM_UNSUBSCRIBED,
        val pollsCount: Int = 0,
        val isEditMode: Boolean = false,
        val isEditRemoveVisible: Boolean = false
    ) : EditPackViewState
}

@HiltViewModel
class EditPackViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val customPacksRepository: CustomPacksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<EditPackInput, EditPackViewState>(
    initialState = EditPackViewState.Loading
) {

    companion object {
        const val POLLS_MAXIMUM_UNSUBSCRIBED = 5
        const val POLLS_MAXIMUM_SUBSCRIBED = 100
    }

    private var packId: Long = -1
    private var title = ""
    private var shareLink: String = ""
    private val polls = mutableListOf<Poll>()

    private var isPackFetched = false

    private var isEditMode = false
    private var removePollsIds = mutableListOf<Long>()

    override fun onViewInitialized(input: EditPackInput) {
        super.onViewInitialized(input)
        packId = input.packId
        title = input.packTitle ?: ""
        shareLink = input.shareLink

        subscribePolls()
    }

    private var pollsSubscription: Job? = null
    private fun subscribePolls() {
        pollsSubscription?.cancel()
        pollsSubscription = null
        pollsSubscription = viewModelScope.launch(ioDispatcher) {
            try {
                customPacksRepository.getPolls(packId = packId)
                    .collect { polls ->
                        isPackFetched = true
                        this@EditPackViewModel.polls.clear()
                        this@EditPackViewModel.polls.addAll(polls)
                        updateView()
                    }
            } catch (ex: Exception) {
                isPackFetched = false
                updateState { EditPackViewState.Error }
                Log.e("Steve", ex.toString())
            }
        }
    }

    private fun updateView() {
        updateState {
            if (!isPackFetched) {
                EditPackViewState.Loading
            } else {
                val limit = if (userRepository.hasSubscription) {
                    POLLS_MAXIMUM_SUBSCRIBED
                } else {
                    POLLS_MAXIMUM_UNSUBSCRIBED
                }

                val items = mutableListOf<PollsItem>()

                items.addAll(
                    polls.map { poll ->
                        val id = poll.id
                        val topText = poll.options[0].title
                        val bottomText = poll.options[1].title

                        PollsItem.TwoOptionsEdit(
                            id = id,
                            left = topText,
                            right = bottomText,
                            isSelected = if (isEditMode) removePollsIds.contains(id) else null
                        )
                    }
                )

                if (polls.size >= limit) {
                    items.add(PollsItem.Subscribe(onClick = ::onSubscribeClick))
                }

                EditPackViewState.Content(
                    title = title,
                    polls = items,
                    isAddButtonVisible = polls.size < limit,
                    isShareButtonEnabled = polls.isNotEmpty(),
                    isEditButtonEnabled = polls.isNotEmpty(),
                    pollsMax = limit,
                    pollsCount = polls.size,
                    isEditMode = isEditMode,
                    isEditRemoveVisible = removePollsIds.isNotEmpty()
                )
            }
        }
    }

    fun onEditClick() {
        removePollsIds.clear()
        isEditMode = true
        updateView()
    }

    fun onRemovePollsApproved() = try {
        viewModelScope.launch(ioDispatcher) {
            removePollsIds.forEach { pollId ->
                launch {
                    try {
                        customPacksRepository.removePoll(packId, pollId)
                    } catch (ex: Exception) {
                        Log.e("Steve", ex.toString())
                    }
                }.join()
            }
        }.invokeOnCompletion {
            resetEditMode()
            viewModelScope.launch {
                emit(ViewAction.ShowToast("Questions successfully removed"))
            }
        }
    } catch (ex: Exception) {
        Log.e("Steve", ex.toString())
        viewModelScope.launch {
            emit(ViewAction.ShowToast("Questions remove failed :( Try again."))
        }
    }

    fun onEditCancelClick() {
        resetEditMode()
    }

    fun onPollItemSelected(pollId: Long) {
        if (removePollsIds.contains(pollId)) {
            removePollsIds.remove(pollId)
        } else {
            removePollsIds.add(pollId)
        }
        updateView()
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
        }
    }

    fun onBackPressed() {
        if (isEditMode) {
            resetEditMode()
        } else {
            viewModelScope.launch {
                route(Router.Route.Finish)
            }
        }
    }

    fun onTitleChanged(title: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                customPacksRepository.updatePack(packId, title)
                this@EditPackViewModel.title = title
                updateView()
            } catch (ex: Exception) {
                emit(ViewAction.ShowToast("Title update failed :( Try again."))
                Log.e("Steve", ex.toString())
            }
        }
    }

    fun onShareClick(clipBoard: ClipboardManager) {
        viewModelScope.launch {
            val clip: ClipData = ClipData.newPlainText("Polleo Pack Share", shareLink)
            clipBoard.setPrimaryClip(clip)
            emit(ViewAction.ShowToast("Link copied to clipboard!"))
        }
    }

    fun onRetryClick() {
        subscribePolls()
        updateState { EditPackViewState.Loading }
    }

    fun onPackRemoveApproved() {
        viewModelScope.launch(ioDispatcher) {
            try {
                customPacksRepository.removePack(packId)
                emit(ViewAction.ShowToast("Pack successfully removed"))
                route(Router.Route.Finish)
            } catch (ex: Exception) {
                Log.e("Steve", ex.toString())
                emit(ViewAction.ShowToast("Pack remove failed :( Try again."))
            }
        }
    }

    fun onAddPollClick() {
        viewModelScope.launch {
            route(Router.Route.EditPoll(EditPollVariant.CustomPackAdd(packId)))
        }
    }

    private fun resetEditMode() {
        isEditMode = false
        removePollsIds.clear()
        updateView()
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            route(Router.Route.SubscriptionPaywall)
        }
    }

    fun onPollClick(id: Long) {
        if (isEditMode) {
            onPollItemSelected(id)
        } else {
            viewModelScope.launch {
                route(Router.Route.Statistic(
                    PollStatisticInput(
                        pollId = id,
                        shareLink = shareLink,
                        isCustomPack = true
                    )
                ))
            }
        }
    }

}