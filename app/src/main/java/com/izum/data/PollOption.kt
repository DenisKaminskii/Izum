package com.izum.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PollOption(
    val id: Long,
    val title: String,
    val votesCount: Long,
    val createdAt: String? = null
) : Parcelable