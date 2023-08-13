package com.izum.domain.core

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import com.izum.ui.ViewAction
import com.izum.ui.route.Router
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class StateViewModel<Arguments ,ViewState>(
    private val initialState: ViewState
) : ViewModel() {

    private val _viewStateFlow = MutableSharedFlow<ViewState>(replay = 1)
    val viewStateFlow: SharedFlow<ViewState>
        get() = _viewStateFlow

    private val _viewActionsFlow = MutableSharedFlow<ViewAction>()
    val viewActionsFlow: SharedFlow<ViewAction>
        get() = _viewActionsFlow

    private val _routeFlow = MutableSharedFlow<Router.Route>()
    val routeFlow: SharedFlow<Router.Route>
        get() = _routeFlow

    val viewState: ViewState
        get() = _viewStateFlow.replayCache.singleOrNull() ?: initialState

    private val updateMutex = Mutex()

    open fun onViewInitialized(args: Arguments) {
        updateState { initialState }
    }

    @WorkerThread
    protected fun updateState(updateFunc: (ViewState) -> ViewState) = runBlocking {
        updateMutex.withLock {
            val newUiState = updateFunc.invoke(viewState)
            _viewStateFlow.emit(newUiState)
        }
    }

    suspend fun route(route: Router.Route) {
        _routeFlow.emit(route)
    }

    suspend fun emit(viewAction: ViewAction) {
        _viewActionsFlow.emit(viewAction)
    }

}