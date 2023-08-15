package com.izum.ui.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.repository.UserRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.NullPointerException
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

    override fun onViewInitialized(args: Unit) {
        super.onViewInitialized(args)
        authorize()
    }

    private fun authorize() = viewModelScope.launch {
        try {
            userRepository.fetchToken()
            if (userRepository.isStatisticInfoProvided) {
                route(Router.Route.Packs)
            } else {
                route(Router.Route.ProvideUserInfo)
            }
        } catch (ex: Exception) {
            Log.e("Steve", ex.toString())
            isLoading = false
            updateView()
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
        authorize()
    }

}