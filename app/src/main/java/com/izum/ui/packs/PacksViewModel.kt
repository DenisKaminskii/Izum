package com.izum.ui.packs

import androidx.lifecycle.viewModelScope
import com.izum.data.Mock
import com.izum.data.Pack
import com.izum.data.repository.PacksRepository
import com.izum.data.repository.UserRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PacksViewState {
    object Loading : PacksViewState
    data class Packs(
        val packs: List<Pack>,
        val hasSubscription: Boolean
    ) : PacksViewState
}


@HiltViewModel
class PacksViewModel @Inject constructor(
    private val packsRepository: PacksRepository,
    private val userRepository: UserRepository
) : StateViewModel<Unit, PacksViewState>(
    initialState = PacksViewState.Loading
) {

    private val packs = mutableListOf<Pack>()
    private val hasSubscription: Boolean
        get() = userRepository.hasSubscription

    override fun onViewInitialized(args: Unit) {
        super.onViewInitialized(args)
        viewModelScope.launch {
            packsRepository.getPacks()
                .collect { packs ->
                    this@PacksViewModel.packs.clear()
                    this@PacksViewModel.packs.addAll(packs)
                    updateView()
                }
        }
    }

    private fun updateView() {
        updateState {
            PacksViewState.Packs(
                packs = packs,
                hasSubscription = hasSubscription
            )
        }
    }

    fun onPackClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.Pack(pack))
        }
    }

    fun onStartClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.Polls(pack))
        }
    }

    fun onPackHistoryClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.PackHistory(pack))
        }
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            userRepository.hasSubscription = !userRepository.hasSubscription
            updateView()
        }
    }

    fun onCreatePollClick() {
        viewModelScope.launch {
            route(Router.Route.CreatePoll)
        }
    }

}