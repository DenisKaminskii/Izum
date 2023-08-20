package com.izum.ui.create.pack

import androidx.lifecycle.viewModelScope
import com.izum.data.CreatePoll
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
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
        val visibleIndex: Int,
        val visibleLastIndex: Int
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
        visibleIndex = 1,
        visibleLastIndex = 1
    )
) {

    companion object {
        private const val MAX_POLLS_COUNT = 10
    }

    private val polls = mutableListOf(
        CreatePoll("", "")
    )

    private var title: String = ""
    private var index = 0

    private val poll: CreatePoll
        get() = polls[index]

    private val topText: String
        get() = poll.topText

    private val bottomText: String
        get() = poll.bottomText

    private val isPollOptionsFilled: Boolean
        get() = topText.isNotBlank() && bottomText.isNotBlank()

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Finish)
        }
    }

    fun onDoneClick() {
        updateState { CreatePackViewState.Loading }
        // TODO:
    }

    private fun updateView() {
        updateState {
            CreatePackViewState.Input(
                topText = topText,
                bottomText = bottomText,
                isDoneEnabled = polls.none { it.topText.isBlank() || it.bottomText.isBlank() } && title.isNotBlank(),
                prevButton = if (index == 0) CreatePackViewState.Button.HIDDEN else CreatePackViewState.Button.ENABLED,
                nextButton = when {
                    index == polls.lastIndex && index < MAX_POLLS_COUNT -> CreatePackViewState.Button.CREATE_NEW
                    index < polls.lastIndex -> CreatePackViewState.Button.ENABLED
                    else -> CreatePackViewState.Button.HIDDEN
                },
                visibleIndex = index + 1,
                visibleLastIndex = polls.lastIndex + 1
            )
        }
    }

    fun onTitleTextChanged(text: String) {
        if (text == title) return
        title = text
        updateView()
    }

    fun onTopTextChanged(text: String) {
        if (text == topText) return
        polls[index] = polls[index].copy(topText = text)
        updateView()
    }

    fun onBottomTextChanged(text: String) {
        if (text == bottomText) return
        polls[index] = polls[index].copy(bottomText = text)
        updateView()
    }

    private var isSliderTracking = false

    fun onSliderTrackingStart() {
        isSliderTracking = true
        updateView()
    }

    fun onSliderTrackingStop() {
        isSliderTracking = false
        updateView()
    }

    fun onSliderChanged(index: Int) {
        this.index = index
        updateView()
    }

    fun onPrevClick(state: CreatePackViewState.Button) {
        if (!isPollOptionsFilled) {
            viewModelScope.launch {
                emit(ViewAction.ShowToast("Fill the current poll"))
            }
            return
        }
        
        when(state) {
            CreatePackViewState.Button.ENABLED -> {
                index--
                updateView()
            }
            else -> {}
        }
    }

    fun onNextClick(state: CreatePackViewState.Button) {
        if (!isPollOptionsFilled) {
            viewModelScope.launch {
                emit(ViewAction.ShowToast("Fill the current poll"))
            }
            return
        }

        when(state) {
            CreatePackViewState.Button.ENABLED -> {
                index++
                updateView()
            }
            CreatePackViewState.Button.CREATE_NEW -> {
                polls.add(CreatePoll("", ""))
                index = polls.lastIndex
                updateView()
            }
            else -> {}
        }
    }

}