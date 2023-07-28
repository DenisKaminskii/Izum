package com.izum.ui.packs

import androidx.lifecycle.viewModelScope
import com.izum.domain.core.StateViewModel
import com.izum.data.packs.Pack
import com.izum.data.packs.PacksRepository
import com.izum.data.packs.ifNoData
import com.izum.data.packs.ifPacks
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
            packsRepository.collect { packsState ->
                packsState
                    .ifNoData { fetchPacks() }
                    .ifPacks { packs ->
                        updateState {
                            PacksViewState.Packs(
                                packs = packs.general
                            )
                        }
                    }
            }
        }

        fetchPacks()
    }

    private fun fetchPacks() {
        viewModelScope.launch{
            packsRepository.fetchPacks()
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