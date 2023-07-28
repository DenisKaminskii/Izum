package com.izum.domain.core

import androidx.annotation.WorkerThread

fun interface UseCase<Output, Params> {

    @WorkerThread
    suspend fun execute(params: Params): Output

}