package com.izum.ui.create

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.SuggestPoll
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

sealed interface EditPollViewState {

    object Loading : EditPollViewState

    data class Input(
        val isDoneEnabled: Boolean
    ) : EditPollViewState
}

sealed interface EditPollVariant : Parcelable {

    @Parcelize
    object PackAdd : EditPollVariant

    @Parcelize
    data class PackEdit(
        val topText: String,
        val bottomText: String
    ) : EditPollVariant

    @Parcelize
    object Suggest : EditPollVariant
}

@HiltViewModel
class EditPollViewModel @Inject constructor(
    private val pollsRepository: PollsRepository
) : StateViewModel<EditPollVariant, EditPollViewState>(
    initialState = EditPollViewState.Input(
        isDoneEnabled = false
    )
) {

    private var suggestPoll = SuggestPoll(
        topText = "",
        bottomText = ""
    )

    private var inputArgs: EditPollVariant = EditPollVariant.Suggest

    override fun onViewInitialized(args: EditPollVariant) {
        super.onViewInitialized(args)
        inputArgs = args
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
        }
    }

    fun onActionClick() {
        updateState { EditPollViewState.Loading }

        when(inputArgs) {
            EditPollVariant.Suggest -> viewModelScope.launch {
                try {
                    pollsRepository.suggestPoll(suggestPoll.topText, suggestPoll.bottomText)
                    emit(ViewAction.ShowToast("Thanks! We will check your poll soon :3"))
                    route(Router.Route.Finish)
                } catch (exception: Exception) {
                    Log.e("Steve", "CreatePollViewModel: $exception")
                    emit(ViewAction.ShowToast("Sorry. Try Again"))
                    updateState { EditPollViewState.Input(isDoneEnabled = true) }
                }
            }
            else -> {}
        }
    }

    fun onTopTextChanged(text: String) {
        suggestPoll = suggestPoll.copy(topText = text)
        updateState {
            EditPollViewState.Input(
                isDoneEnabled = suggestPoll.topText.isNotBlank() && suggestPoll.bottomText.isNotBlank()
            )
        }
    }

    fun onBottomTextChanged(text: String) {
        suggestPoll = suggestPoll.copy(bottomText = text)
        updateState {
            EditPollViewState.Input(
                isDoneEnabled = suggestPoll.topText.isNotBlank() && suggestPoll.bottomText.isNotBlank()
            )
        }
    }

}