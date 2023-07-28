package com.izum.ui.poll

import androidx.lifecycle.viewModelScope
import com.izum.data.packs.PacksRepository
import com.izum.data.packs.ifNoData
import com.izum.data.packs.ifPacks
import com.izum.domain.core.StateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.izum.ui.poll.PollViewModel.Companion.Arguments as Arguments

sealed interface PollViewState {
    object Loading : PollViewState
    data class Pack(
        val pack: com.izum.data.packs.Pack
    ) : PollViewState
}

@HiltViewModel
class PollViewModel @Inject constructor(
    private val packsRepository: PacksRepository
) : StateViewModel<Arguments, PollViewState>(
    initialState = PollViewState.Loading
) {

    companion object {
        data class Arguments(
            val packId: Long
        )
    }

    override fun init(args: Arguments) {
        super.init(args)
        viewModelScope.launch {
            packsRepository.fetchPackPolls(args.packId)
            packsRepository.collect { state ->
                state
                    .ifPacks {
                        // emits
                    }
            }
        }
    }

}

fun PollViewState.ifPack(func: (state: PollViewState.Pack) -> Unit): PollViewState {
    if (this is PollViewState.Pack) {
        func.invoke(this)
    }
    return this
}

fun PollViewState.ifLoading(func: (state: PollViewState.Loading) -> Unit): PollViewState {
    if (this is PollViewState.Loading) {
        func.invoke(this)
    }
    return this
}