package com.polleo.ui.custom

import androidx.lifecycle.viewModelScope
import com.polleo.data.repository.CustomPacksRepository
import com.polleo.domain.core.StateViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.Throws

sealed class AddCustomPackViewState {
    object Default : AddCustomPackViewState()
    object Error : AddCustomPackViewState()
    object Loading : AddCustomPackViewState()
    data class Success(
        val title: String,
        val pollsCount: Int
    ) : AddCustomPackViewState()
}

@HiltViewModel
class AddCustomPackViewModel @Inject constructor(
    private val customPacksRepository: CustomPacksRepository
) : StateViewModel<Unit, AddCustomPackViewState>(
    initialState = AddCustomPackViewState.Default
) {

    fun onAddCode(code: String) = viewModelScope.launch {
        val codeData = try {
            code.retrieveCodeData()
        } catch (e: IllegalArgumentException) {
            updateState { AddCustomPackViewState.Error }
            return@launch
        }

        updateState { AddCustomPackViewState.Loading }

        delay(2_000)

        updateState {
            AddCustomPackViewState.Success(
                title = "Opros perduna",
                pollsCount = 37
            )
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun String.retrieveCodeData() : Pair<Int, String> {
        val exception = IllegalArgumentException("Invalid code")
        val split = split("-")
        if (split.size < 2) throw exception

        val id = split[0].toIntOrNull() ?: throw exception
        val name = split[1].takeIf { it.isNotBlank() } ?: throw exception

        return id to name
    }

}