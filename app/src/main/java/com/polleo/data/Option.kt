package com.polleo.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Option(
    val id: String,
    val title: String,
    val votes: Long
) : Parcelable