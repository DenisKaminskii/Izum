package com.izum.ui.main

import androidx.lifecycle.viewModelScope
import com.izum.data.repository.UserRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
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
                // § route(Router.Route.Packs)
                route(Router.Route.Onboarding)
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