package com.izum.data

import android.os.Parcelable
import com.izum.api.PollJson
import kotlinx.parcelize.Parcelize

@Parcelize
data class Poll(
    val id: Long,
    val packId: Long,
    val author: Author? = null,
    val options: List<PollOption>,
    val totalVotesCount: Long? = null,
    val voted: Vote? = null,
    val createdAt: String? = null
) : Parcelable {

    companion object {
        fun fromJson(json: PollJson): Poll {
            return Poll(
                id = json.id,
                packId = json.packId,
                author = json.author?.let { Author(it.id) },
                options = json.options.map {
                    PollOption(
                        id = it.id,
                        title = it.title,
                        votesCount = it.votesCount,
                        createdAt = it.createdAt
                    )
                },
                totalVotesCount = json.totalVotesCount,
                voted = json.voted?.let {
                    Vote(
                        optionId = it.optionId,
                        date = it.date
                    )
                },
                createdAt = json.createdAt
            )
        }
    }

}