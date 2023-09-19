package com.izum.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.EditPoll
import com.izum.data.Poll
import com.izum.data.repository.CustomPacksRepository
import com.izum.data.repository.UserRepository
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.create.EditPackViewModel.Companion.POLLS_MAXIMUM_UNSUBSCRIBED
import com.izum.ui.edit.EditPollVariant
import com.izum.ui.poll.list.PollsItem
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
        val pollsMax: Int = POLLS_MAXIMUM_UNSUBSCRIBED,
        val pollsCount: Int = 0
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
                val max = if (userRepository.hasSubscription) {
                    POLLS_MAXIMUM_SUBSCRIBED
                } else {
                    POLLS_MAXIMUM_UNSUBSCRIBED
                }

                EditPackViewState.Content(
                    title = title,
                    polls = polls.map { poll ->
                        val id = poll.id
                        val topText = poll.options[0].title
                        val bottomText = poll.options[1].title
                        PollsItem.TwoOptionsEdit(id, topText, bottomText)
                    },
                    isAddButtonVisible = polls.size < max,
                    isShareButtonEnabled = polls.isNotEmpty(),
                    pollsMax = max,
                    pollsCount = polls.size,
                )
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
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

    fun onRemoveApprovedClick() {
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

    fun onPollClick(id: Long) {
        viewModelScope.launch {
            route(Router.Route.Statistic(id))
        }
    }

    fun onPollRemove(id: Long) {

    }

}