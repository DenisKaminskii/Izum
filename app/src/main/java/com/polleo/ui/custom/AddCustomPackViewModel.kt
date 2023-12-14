package com.polleo.ui.custom

import androidx.lifecycle.viewModelScope
import com.polleo.data.Pack
import com.polleo.data.repository.CustomPacksRepository
import com.polleo.data.retrieveCodeData
import com.polleo.di.IoDispatcher
import com.polleo.domain.core.StateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.jvm.Throws

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