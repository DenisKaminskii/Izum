package com.polleo.ui

sealed interface ViewAction {
    data class ShowToast(val message: String) : ViewAction
}