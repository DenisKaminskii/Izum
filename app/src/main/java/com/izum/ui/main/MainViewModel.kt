package com.izum.ui.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.data.repository.UserRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : StateViewModel<Unit, Unit>(
    initialState = Unit
) {

    override fun onViewInitialized(args: Unit) {
        super.onViewInitialized(args)
        viewModelScope.launch {
            try {
                userRepository.fetchToken()
                route(Router.Route.Packs)
            } catch (ex: Exception) {
                Log.e("Steve", ex.toString())
                // TODO: Show retry button
            }
        }
    }

}