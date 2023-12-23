package com.pickone.network

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

interface OnUserIdUpdated : MutableSharedFlow<Long>

class OnUserIdUpdatedImpl @Inject constructor() : OnUserIdUpdated,
    MutableSharedFlow<Long> by MutableSharedFlow()