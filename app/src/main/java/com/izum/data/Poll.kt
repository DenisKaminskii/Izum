package com.izum.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Poll(
    @PrimaryKey val id: Long,
    @ColumnInfo("packId") val packId: Long,
    @ColumnInfo("options") val options: List<PollOption>,
    @ColumnInfo("votedOptionId") val votedOptionId: Long? = null
)

@Entity
data class PollOption(
    @PrimaryKey val id: Long,
    @ColumnInfo("title") val title: String,
    @ColumnInfo("votesCount") val votesCount: Long
)