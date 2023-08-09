package com.izum.data

data class Poll(
    val id: Long,
    val packId: Long,
    val options: List<PollOption>,
    val votedOptionId: Long? = null
)

data class PollOption(
    val id: Long,
    val title: String,
    val votesCount: Long
)