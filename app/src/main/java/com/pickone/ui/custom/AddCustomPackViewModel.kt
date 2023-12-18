package com.pickone.ui.custom

import androidx.lifecycle.viewModelScope
import com.pickone.data.Pack
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.retrieveCodeData
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.StateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddCustomPackViewState {
    object Default : AddCustomPackViewState()
    object InvalidCode : AddCustomPackViewState()
    object NotFound : AddCustomPackViewState()
    object Loading : AddCustomPackViewState()
    data class Success(
        val pack: Pack.Custom
    ) : AddCustomPackViewState()
}

@HiltViewModel
class AddCustomPackViewModel @Inject constructor(
    private val customPacksRepository: CustomPacksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<Unit, AddCustomPackViewState>(
    initialState = AddCustomPackViewState.Default
) {

    fun onAddCode(code: String) = viewModelScope.launch(ioDispatcher) {
        val codeData = code.retrieveCodeData()
        if (codeData == null) {
            updateState { AddCustomPackViewState.InvalidCode }
            return@launch
        }

        updateState { AddCustomPackViewState.Loading }

        val customPack = customPacksRepository.getCustomPack(codeData.first, codeData.second)
        if (customPack != null) {
            updateState {
                AddCustomPackViewState.Success(customPack)
            }
        } else {
            updateState {
                AddCustomPackViewState.NotFound
            }
        }
    }

}