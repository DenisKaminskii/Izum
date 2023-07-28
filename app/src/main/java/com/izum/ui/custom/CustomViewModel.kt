package com.izum.ui.custom

import androidx.lifecycle.viewModelScope
import com.izum.data.packs.Pack
import com.izum.data.packs.PacksRepository
import com.izum.data.packs.ifNoData
import com.izum.data.packs.ifPacks
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CustomViewState {
    object Loading : CustomViewState
    data class Packs(
        val packs: List<Pack>
    ) : CustomViewState
}

@HiltViewModel
class CustomViewModel @Inject constructor(
    private val packsRepository: PacksRepository
) : StateViewModel<Unit, CustomViewState>(
    initialState = CustomViewState.Loading
) {

    override fun init(args: Unit) {
        super.init(args)
        viewModelScope.launch {
            packsRepository.collect { packsState ->
                packsState
                    .ifNoData { fetchPacks() }
                    .ifPacks { packs ->
                        updateState {
                            CustomViewState.Packs(
                                packs = packs.custom
                            )
                        }
                    }
            }
        }

        // no fetch packs
    }

    private fun fetchPacks() {
        viewModelScope.launch{
            packsRepository.fetchPacks()
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            route(Router.Route.Packs)
        }
    }

    fun onPackClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.Poll(pack))
        }
    }

}