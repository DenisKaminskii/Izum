package com.izum.ui.create

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.repository.PollsRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreatePollViewState {
    object Loading : CreatePollViewState
    data class Input(
        val isDoneEnabled: Boolean
    ) : CreatePollViewState
}

@HiltViewModel
class CreatePollViewModel @Inject constructor(
    private val pollsRepository: PollsRepository
) : StateViewModel<Unit, CreatePollViewState>(
    initialState = CreatePollViewState.Input(
        isDoneEnabled = false
    )
) {

    private var topText = ""
    private var bottomText = ""

    override fun init(args: Unit) {
        super.init(args)
        // ..
    }

    fun onBackClick() {
        viewModelScope.launch {
            emit(ViewAction.Finish)
        }
    }

    fun onDoneClick() {
        updateState { CreatePollViewState.Loading }
        viewModelScope.launch {
            try {
                pollsRepository.suggest()
                emit(ViewAction.Finish)
            } catch (exception: Exception) {
                Log.e("Steve", "CreatePollViewModel: $exception")
                emit(ViewAction.ShowToast("Sorry. Try Again"))
                updateState { CreatePollViewState.Input(isDoneEnabled = true) }
            }
        }
    }

    fun onTopTextChanged(text: String) {
        topText = text
        updateState {
            CreatePollViewState.Input(
                isDoneEnabled = topText.isNotBlank() && bottomText.isNotBlank()
            )
        }
    }

    fun onBottomTextChanged(text: String) {
        bottomText = text
        updateState {
            CreatePollViewState.Input(
                isDoneEnabled = topText.isNotBlank() && bottomText.isNotBlank()
            )
        }
    }

}