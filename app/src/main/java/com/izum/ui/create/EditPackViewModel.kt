package com.izum.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.EditPoll
import com.izum.data.repository.CustomPacksRepository
import com.izum.data.repository.PollsRepository
import com.izum.data.repository.UserRepository
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.create.EditPackViewModel.Companion.POLLS_MAXIMUM_UNSUBSCRIBED
import com.izum.ui.poll.list.PollsItem
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
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
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val pollsRepository: PollsRepository,
    private val customPacksRepository: CustomPacksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<EditPackInput, EditPackViewState>(
    initialState = EditPackViewState.Loading
) {

    companion object {
        const val POLLS_MAXIMUM_UNSUBSCRIBED = 10
        const val POLLS_MAXIMUM_SUBSCRIBED = 100
    }

    private var packId: Long = -1
    private var title = ""
    private var shareLink: String = ""
    private val polls = mutableListOf<EditPoll>()

    private var isPackFetched = false

    override fun onViewInitialized(input: EditPackInput) {
        super.onViewInitialized(input)
        packId = input.packId
        title = input.packTitle ?: ""
        shareLink = input.shareLink

        if (input.isNew) {
            isPackFetched = true
            updateView()
        } else {
            fetchPolls()
        }
    }

    private fun fetchPolls() = viewModelScope.launch(ioDispatcher) {
        try {
            val polls = pollsRepository.getPackPolls(packId = packId)
            this@EditPackViewModel.polls.clear()
            this@EditPackViewModel.polls.addAll(polls.map {
                EditPoll(
                    topText = it.options[0].title,
                    bottomText = it.options[1].title
                )
            })
            isPackFetched = true
            updateView()
        } catch (ex: Exception) {
            isPackFetched = false
            updateState { EditPackViewState.Error }
            Log.e("Steve", ex.toString())
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
                    polls = polls
                        .map { PollsItem.TwoOptionsEdit(it.topText, it.bottomText) }
                        .let { packPolls ->
                            if (packPolls.size >= max) {
                                packPolls
                            } else {
                                packPolls + listOf(
                                    PollsItem.Button(
                                        title = "Add poll +",
                                        onClick = { onAddPollClick() }
                                    )
                                )
                            }
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
        fetchPolls()
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

    }

    fun onSaveClick() {

    }
}