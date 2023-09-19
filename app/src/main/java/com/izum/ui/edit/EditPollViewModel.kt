package com.izum.ui.edit

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import com.izum.R
import com.izum.data.EditPoll
import com.izum.data.repository.CustomPacksRepository
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EditPollViewState {

    object Loading : EditPollViewState

    data class Input(
        val title: String = "Suggest Poll",
        val isDoneEnabled: Boolean = false,
        val actionText: String = "",
        @DrawableRes val actionDrawableId: Int = R.drawable.ic_send_16,
        val suggestText: String = ""
    ) : EditPollViewState
}

// Короче Влад в итоге не возращает список. Он возращает этот же опрос но полноценный.
// В целом оставляем все как есть, просто надо обработать что не надо обновлять список вcех опросов, который мы на старте получаем.
@HiltViewModel
class EditPollViewModel @Inject constructor(
    private val pollsRepository: PollsRepository,
    private val customPacksRepository: CustomPacksRepository
) : StateViewModel<EditPollVariant, EditPollViewState>(
    initialState = EditPollViewState.Input()
) {

    private var editPoll = EditPoll(
        topText = "",
        bottomText = ""
    )

    private var inputArgs: EditPollVariant = EditPollVariant.Suggest
    
    private val actionTitle: String
        get() = when (inputArgs) {
            is EditPollVariant.Suggest -> "SEND"
            is EditPollVariant.CustomPackAdd -> "ADD"
        }
    private val actionDrawableId: Int
        get() = when (inputArgs) {
            is EditPollVariant.Suggest -> R.drawable.ic_send_16
            is EditPollVariant.CustomPackAdd -> R.drawable.ic_add_16
        }

    private var actionJob: Job? = null

    override fun onViewInitialized(input: EditPollVariant) {
        super.onViewInitialized(input)
        inputArgs = input
        updateView()
    }

    private fun updateView() {
        updateState {
            val isBothTextAreNotEmpty = editPoll.topText.isNotBlank() && editPoll.bottomText.isNotBlank()

            EditPollViewState.Input(
                title = when (inputArgs) {
                    is EditPollVariant.Suggest -> "Suggest poll"
                    is EditPollVariant.CustomPackAdd -> "Add poll"
                },
                isDoneEnabled = isBothTextAreNotEmpty || actionJob?.isActive == true,
                actionText = actionTitle,
                actionDrawableId = actionDrawableId,
                suggestText = when (inputArgs) {
                    is EditPollVariant.Suggest -> "Suggest your poll to us. We will check it and add to the app if like it :3"
                    is EditPollVariant.CustomPackAdd -> "Add new poll to the pack"
                }
            )
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
        }
    }

    fun onActionClick() {
        updateState { EditPollViewState.Loading }

        if (actionJob?.isActive == true) return

        actionJob = when(inputArgs) {
            is EditPollVariant.Suggest -> viewModelScope.launch {
                try {
                    pollsRepository.suggestPoll(editPoll)
                    emit(ViewAction.ShowToast("Thanks! We will check your poll soon :3"))
                    route(Router.Route.Finish)
                } catch (exception: Exception) {
                    Log.e("Steve", "CreatePollViewModel: $exception")
                    emit(ViewAction.ShowToast("Sorry. Try Again"))
                    updateView()
                }
            }
            is EditPollVariant.CustomPackAdd -> viewModelScope.launch {
                try {
                    val packId = (inputArgs as? EditPollVariant.CustomPackAdd)?.packId ?: return@launch
                    customPacksRepository.addPoll(packId, editPoll)
                    emit(ViewAction.ShowToast("Poll added"))
                    route(Router.Route.Finish)
                } catch (exception: Exception) {
                    Log.e("Steve", "CreatePollViewModel: $exception")
                    emit(ViewAction.ShowToast("Sorry. Try Again"))
                    updateView()
                }
            }
        }
    }

    fun onTopTextChanged(text: String) {
        editPoll = editPoll.copy(topText = text)
        updateView()
    }

    fun onBottomTextChanged(text: String) {
        editPoll = editPoll.copy(bottomText = text)
        updateView()
    }

}