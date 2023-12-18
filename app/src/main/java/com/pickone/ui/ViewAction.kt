package com.pickone.ui

sealed interface ViewAction {
    data class ShowToast(val message: String) : ViewAction
}