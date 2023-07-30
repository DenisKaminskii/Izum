package com.izum.ui

data class SliderViewState(
    val lastIndex: Int,
    val index: Int,
    val isTracking: Boolean,
    val lastAvailableIndex: Int?
)