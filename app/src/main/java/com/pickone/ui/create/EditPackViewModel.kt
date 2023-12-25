package com.pickone.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import timber.log.Timber
import androidx.lifecycle.viewModelScope
import com.pickone.data.Poll
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.UserRepository
import com.pickone.data.retrieveCodeData
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.ViewAction
import com.pickone.ui.create.EditPackViewModel.Companion.POLLS_MAXIMUM_UNSUBSCRIBED
import com.pickone.ui.edit.EditPollVariant
import com.pickone.ui.poll.list.PollsItem
import com.pickone.ui.poll.statistic.PollStatisticInput
import com.pickone.ui.route.Router
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
    private var packTitle = ""
    private var packCode: String = ""
    private val polls = mutableListOf<Poll>()

    private var isPackFetched = false

    private var isEditMode = false
    private var removePollsIds = mutableListOf<Long>()

    override fun onViewInitialized(input: EditPackInput) {
        super.onViewInitialized(input)
        packId = input.packId
        packTitle = input.packTitle ?: ""
        packCode = input.packCode

        subscribePolls()
    }

    private var pollsSubscription: Job? = null
    private fun subscribePolls() {
        pollsSubscription?.cancel()
        pollsSubscription = null
        pollsSubscription = viewModelScope.launch(ioDispatcher) {
            try {
                val codeData = packCode.retrieveCodeData() ?: throw Exception("Couldn't parse the pack code")
                customPacksRepository.getMyPackPolls(packId = codeData.first, packToken = codeData.second)
                    .collect { polls ->
                        isPackFetched = true
                        this@EditPackViewModel.polls.clear()
                        this@EditPackViewModel.polls.addAll(polls)
                        updateView()
                    }
            } catch (ex: Exception) {
                isPackFetched = false
                updateState { EditPackViewState.Error }
                Timber.e(ex, "Failed to fetch polls")
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
                    title = packTitle,
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
                        Timber.e(ex, "Failed to remove poll")
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
        Timber.e(ex, "Something went wrong when removing polls")
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
                this@EditPackViewModel.packTitle = title
                updateView()
            } catch (ex: Exception) {
                emit(ViewAction.ShowToast("Title update failed :( Try again."))
                Timber.e( ex.toString())
            }
        }
    }

    fun onShareClick(clipBoard: ClipboardManager) {
        viewModelScope.launch {
            val clip: ClipData = ClipData.newPlainText("Polleo Pack Share", packCode)
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
                Timber.e( ex.toString())
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
            route(Router.Route.Paywall)
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
                        shareLink = packCode,
                        isCustomPack = true
                    )
                ))
            }
        }
    }

}