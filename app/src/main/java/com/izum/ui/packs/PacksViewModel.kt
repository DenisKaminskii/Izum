package com.izum.ui.packs

import androidx.lifecycle.viewModelScope
import com.izum.data.Pack
import com.izum.data.repository.PacksRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PacksViewState {
    object Loading : PacksViewState
    data class Packs(
        val packs: List<Pack>
    ) : PacksViewState
}


@HiltViewModel
class PacksViewModel @Inject constructor(
    private val packsRepository: PacksRepository
) : StateViewModel<Unit, PacksViewState>(
    initialState = PacksViewState.Loading
) {

    override fun init(args: Unit) {
        super.init(args)
        viewModelScope.launch {
            packsRepository.getPacks()
                .collect { packs ->
                    updateState { PacksViewState.Packs(packs) }
                }
        }
    }

    fun onCustomClick() {
        viewModelScope.launch {
            route(Router.Route.Custom)
        }
    }

    fun onPackClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.Poll(pack))
        }
    }

}