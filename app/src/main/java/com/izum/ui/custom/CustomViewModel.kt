package com.izum.ui.custom

import androidx.lifecycle.viewModelScope
import com.izum.data.Pack
import com.izum.data.repository.PacksRepository
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
            packsRepository.getCustomPacks()
                .collect { packs->
                    updateState { CustomViewState.Packs(packs) }
                }
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