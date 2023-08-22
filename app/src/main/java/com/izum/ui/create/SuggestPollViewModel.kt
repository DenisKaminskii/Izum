package com.izum.ui.create

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.CreatePoll
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SuggestPollViewState {

    object Loading : SuggestPollViewState

    data class Input(
        val isDoneEnabled: Boolean
    ) : SuggestPollViewState
}

@HiltViewModel
class SuggestPollViewModel @Inject constructor(
    private val pollsRepository: PollsRepository
) : StateViewModel<Unit, SuggestPollViewState>(
    initialState = SuggestPollViewState.Input(
        isDoneEnabled = false
    )
) {

    private var createPoll = CreatePoll(
        topText = "",
        bottomText = ""
    )

    override fun onViewInitialized(args: Unit) {
        super.onViewInitialized(args)
        // ..
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
        }
    }

    fun onDoneClick() {
        updateState { SuggestPollViewState.Loading }
        viewModelScope.launch {
            try {
                ///pollsRepository.suggest()
                route(Router.Route.Finish)
            } catch (exception: Exception) {
                Log.e("Steve", "CreatePollViewModel: $exception")
                emit(ViewAction.ShowToast("Sorry. Try Again"))
                updateState { SuggestPollViewState.Input(isDoneEnabled = true) }
            }
        }
    }

    fun onTopTextChanged(text: String) {
        createPoll = createPoll.copy(topText = text)
        updateState {
            SuggestPollViewState.Input(
                isDoneEnabled = createPoll.topText.isNotBlank() && createPoll.bottomText.isNotBlank()
            )
        }
    }

    fun onBottomTextChanged(text: String) {
        createPoll = createPoll.copy(bottomText = text)
        updateState {
            SuggestPollViewState.Input(
                isDoneEnabled = createPoll.topText.isNotBlank() && createPoll.bottomText.isNotBlank()
            )
        }
    }

}