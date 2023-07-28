package com.izum.domain.core

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class MutableStateProducer<State>(
    private val initialValue: State
) : StateProducer<State> {

    private val stateFlow = MutableSharedFlow<State>(replay = 1)

    private val updateMutex = Mutex()

    override val state: State
        get() = stateFlow.replayCache.singleOrNull() ?: initialValue

    init {
        updateState { initialValue }
    }

    @WorkerThread
    override suspend fun collect(collector: FlowCollector<State>) {
        stateFlow.collect { collector.emit(it) }
    }

    @WorkerThread
    protected fun updateState(updateFunc: (State) -> State) = runBlocking {
        updateMutex.withLock {
            val newState = updateFunc.invoke(state)
            stateFlow.emit(newState)
        }
    }

}