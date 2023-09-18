package com.izum.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vote(
    val optionId: Long,
    val date: String
) : Parcelable