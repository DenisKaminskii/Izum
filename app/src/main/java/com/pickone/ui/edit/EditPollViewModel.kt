package com.pickone.ui.edit

import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import com.pickone.R
import com.pickone.analytics.Analytics
import com.pickone.data.EditPoll
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.ViewAction
import com.pickone.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
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

@HiltViewModel
class EditPollViewModel @Inject constructor(
    private val publicPacksRepository: PublicPacksRepository,
    private val customPacksRepository: CustomPacksRepository,
    private val analytics: Analytics
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
                    is EditPollVariant.Suggest -> "Suggest question"
                    is EditPollVariant.CustomPackAdd -> "Add question"
                },
                isDoneEnabled = isBothTextAreNotEmpty || actionJob?.isActive == true,
                actionText = actionTitle,
                actionDrawableId = actionDrawableId,
                suggestText = when (inputArgs) {
                    is EditPollVariant.Suggest -> "Suggest your question and it might be featured in the 'Suggests' pack!"
                    is EditPollVariant.CustomPackAdd -> "Add new question to your pack"
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
                    publicPacksRepository.suggestPoll(editPoll)
                    emit(ViewAction.ShowToast("Thanks! We will check soon \uD83D\uDC40"))
                    route(Router.Route.Finish)
                } catch (exception: Exception) {
                    Timber.e(exception, "On suggest poll error")
                    emit(ViewAction.ShowToast("No internet connection \uD83D\uDCE1"))
                    updateView()
                }
            }
            is EditPollVariant.CustomPackAdd -> viewModelScope.launch {
                try {
                    val packId = (inputArgs as? EditPollVariant.CustomPackAdd)?.packId ?: return@launch
                    customPacksRepository.addPoll(packId, editPoll)
                    route(Router.Route.Finish)
                } catch (exception: Exception) {
                    analytics.customPackAddPollError()
                    Timber.e(exception, "On custom pack add poll error")
                    emit(ViewAction.ShowToast("No internet connection \uD83D\uDCE1"))
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