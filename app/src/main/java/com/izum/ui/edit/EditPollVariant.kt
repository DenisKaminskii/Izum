package com.izum.ui.edit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class EditPollVariant : Parcelable {
    @Parcelize
    object Suggest : EditPollVariant()
    @Parcelize
    data class CustomPackAdd(val packId: Long) : EditPollVariant()
}