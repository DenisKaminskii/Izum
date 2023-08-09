package com.izum.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Poll(
    val id: Long,
    val packId: Long,
    val options: List<PollOption>,
    val votedOptionId: Long? = null
) : Parcelable

@Parcelize
data class PollOption(
    val id: Long,
    val title: String,
    val votesCount: Long
) : Parcelable