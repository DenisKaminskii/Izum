package com.izum.ui.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import com.izum.data.user.UserRepository
import com.izum.data.user.ifAuthorized
import com.izum.data.user.ifUnAuthorized
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : StateViewModel<Unit, Unit>(
    initialState = Unit
) {


    override fun init(args: Unit) {
        super.init(args)
        viewModelScope.launch {
            userRepository.collect { userState ->
                userState
                    .ifAuthorized {
                        viewModelScope.launch(Dispatchers.IO) {
                            route(Router.Route.Packs)
                        }
                    }
                    .ifUnAuthorized {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                userRepository.fetchToken()
                            } catch (exception: Exception) {
                                Log.e("Steve", exception.message.toString())
                            }
                        }
                    }
            }
        }
    }

}