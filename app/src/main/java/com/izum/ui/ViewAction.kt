package com.izum.ui

sealed interface ViewAction {
    data class ShowToast(val message: String) : ViewAction
    object Finish : ViewAction
}