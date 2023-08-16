package com.izum.ui.user

import androidx.annotation.IntRange
import androidx.lifecycle.viewModelScope
import com.izum.data.repository.UserRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserInfoViewState(
    val selectedGender: Gender? = null,
    val age: Int? = null,
    val isFinishEnabled: Boolean = false,
    val isLoadingVisible: Boolean = false
)

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userRepository: UserRepository
) : StateViewModel<Unit, UserInfoViewState>(
    initialState = UserInfoViewState()
) {

    companion object {
        const val AGE_LIMIT = 120
    }

    private var selectedGender: Gender? = null
    @IntRange(from = 0, to = AGE_LIMIT.toLong())
    private var selectedAge: Int? = null

    private var updatingJob: Job? = null

    private fun updateView() {
        updateState {
            UserInfoViewState(
                selectedGender = selectedGender,
                age = selectedAge,
                isFinishEnabled = selectedGender != null && selectedAge != null,
                isLoadingVisible = updatingJob?.isActive == true
            )
        }
    }

    fun onGenderSelect(gender: Gender) {
        selectedGender = gender
        updateView()
    }

    fun onAgeInput(age: Int?) {
        selectedAge = age
        updateView()
    }

    fun onFinishClick() {
        val gender = selectedGender
        val age = selectedAge

        if(gender == null || age == null) {
            viewModelScope.launch {
                emit(ViewAction.ShowToast("Please, fill the form :3"))
            }
            return
        }

        updatingJob = viewModelScope.launch {
            delay(2_000)
            val isUserInfoUpdated = userRepository.updateUserInfo(age, gender)
            if (isUserInfoUpdated) {
                route(Router.Route.Packs)
            } else {
                emit(ViewAction.ShowToast("Some problems with server :c"))
            }
        }

        updatingJob?.invokeOnCompletion {
            updatingJob = null
            updateView()
        }

        updateView()
    }
}