package com.izum.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    val id: Long
) : Parcelable