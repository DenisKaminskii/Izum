package com.izum.data.packs

data class Pack(
    val id: Long,
    val title: String,
    val description: String,
    val isPaid: Boolean,
    val productId: String?,
    val pollsCount: Long,
    val authorId: Long
)