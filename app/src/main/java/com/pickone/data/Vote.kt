package com.pickone.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vote(
    val optionId: Long,
    val date: String
) : Parcelable