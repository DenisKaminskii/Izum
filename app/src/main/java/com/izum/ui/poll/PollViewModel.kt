package com.izum.ui.poll

import androidx.lifecycle.viewModelScope
import com.izum.data.packs.PacksRepository
import com.izum.domain.core.StateViewModel
import com.izum.ui.poll.PollViewModel.Companion.Arguments
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            val polls = packsRepository.getPackPolls(args.packId)
        }
    }

}