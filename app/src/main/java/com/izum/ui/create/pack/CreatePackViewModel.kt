package com.izum.ui.create.pack

import androidx.lifecycle.viewModelScope
import com.izum.data.CreatePoll
import com.izum.domain.core.StateViewModel
import com.izum.ui.SliderViewState
import com.izum.ui.ViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreatePackViewState {
    object Loading : CreatePackViewState
    data class Input(
        val topText: String,
        val bottomText: String,
        val isDoneEnabled: Boolean,
        val prevButton: Button,
        val nextButton: Button,
        val slider: SliderViewState?
    ) : CreatePackViewState

    enum class Button {
        HIDDEN, ENABLED, DISABLED, CREATE_NEW
    }
}

@HiltViewModel
class CreatePackViewModel @Inject constructor(

) : StateViewModel<Unit, CreatePackViewState>(
    initialState = CreatePackViewState.Input(
        topText = "",
        bottomText = "",
        isDoneEnabled = false,
        prevButton = CreatePackViewState.Button.HIDDEN,
        nextButton = CreatePackViewState.Button.CREATE_NEW,
        slider = null
    )
) {

    private val polls = mutableListOf(
        CreatePoll("", "")
    )
    private var index = 0

    private val poll: CreatePoll
        get() = polls[index]

    private val topText: String
        get() = poll.topText

    private val bottomText: String
        get() = poll.bottomText

    fun onBackClick() {
        viewModelScope.launch {
            emit(ViewAction.Finish)
        }
    }

    fun onDoneClick() {
        updateState { CreatePackViewState.Loading }
        // TODO:
    }

    fun onCreateNewClick() {
        polls.add(CreatePoll("", ""))
        index = polls.lastIndex
        updateState { CreatePackViewState.Input(
            topText = topText,
            bottomText = bottomText,
            isDoneEnabled = false,
            prevButton = CreatePackViewState.Button.ENABLED,
            nextButton = CreatePackViewState.Button.CREATE_NEW,
            slider = null //TODO: (зависит от количества опросов)
        ) }
    }

    fun onTopTextChanged(text: String) {

    }

    fun onBottomTextChanged(text: String) {

    }

}