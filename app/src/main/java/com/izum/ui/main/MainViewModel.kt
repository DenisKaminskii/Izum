package com.izum.ui.main

import androidx.lifecycle.viewModelScope
import com.izum.data.user.UserRepository
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : StateViewModel<Unit, Unit>(
    initialState = Unit
) {

    override fun init(args: Unit) {
        super.init(args)
        viewModelScope.launch {
            try {
                userRepository.fetchToken()
                route(Router.Route.Packs)
            } catch (ex: Exception) {
                // TODO: Show retry button
            }
        }
    }

}