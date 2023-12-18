package com.pickone.ui.main

import androidx.lifecycle.viewModelScope
import com.pickone.data.repository.UserRepository
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainViewState(
    val isLoading: Boolean
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : StateViewModel<Unit, MainViewState>(
    initialState = MainViewState(
        isLoading = true
    )
) {

    private var isLoading = true

    override fun onViewInitialized(input: Unit) {
        super.onViewInitialized(input)

        viewModelScope.launch {
            if (userRepository.isStatisticInfoProvided) {
                route(Router.Route.Packs)
            } else {
                route(Router.Route.ProvideUserInfo)
            }
        }
    }

    private fun updateView() {
        updateState {
            MainViewState(
                isLoading = isLoading
            )
        }
    }

    fun onReloadClick() {
        isLoading = true
        updateView()
    }

}