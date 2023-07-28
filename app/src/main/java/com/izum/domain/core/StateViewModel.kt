package com.izum.domain.core

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.izum.ui.route.Router
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class StateViewModel<Arguments ,ViewState>(
    private val initialState: ViewState
) : ViewModel() {

    private val _uiStateFlow = MutableSharedFlow<ViewState>(replay = 1)
    val uiStateFlow: SharedFlow<ViewState>
        get() = _uiStateFlow

    private val _routeFlow = MutableSharedFlow<Router.Route>()
    val routeFlow: SharedFlow<Router.Route>
        get() = _routeFlow

    val uiState: ViewState
        get() = _uiStateFlow.replayCache.singleOrNull() ?: initialState

    private val updateMutex = Mutex()

    open fun init(args: Arguments) {
        updateState { initialState }
    }

    @WorkerThread
    protected fun updateState(updateFunc: (ViewState) -> ViewState) = runBlocking {
        updateMutex.withLock {
            val newUiState = updateFunc.invoke(uiState)
            _uiStateFlow.emit(newUiState)
        }
    }

    suspend fun route(route: Router.Route) {
        _routeFlow.emit(route)
    }

}