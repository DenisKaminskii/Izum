package com.izum.domain.core

import kotlinx.coroutines.flow.Flow


interface StateProducer<State> : Flow<State> {

    val state: State

}