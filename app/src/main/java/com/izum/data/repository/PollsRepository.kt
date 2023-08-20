package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollApi
import com.izum.api.PollJson
import com.izum.api.VoteRequestJson
import com.izum.data.Poll
import com.izum.data.PollOption
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

interface PollsRepository {

    @WorkerThread
    suspend fun getPackUnvotedPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackVotedPolls(packId: Long): List<Poll>

    @WorkerThread
    @Throws(IOException::class)
    suspend fun vote(pollId: Long, optionId: Long)

}

class PollsRepositoryImpl(
    private val pollsApi: PollApi,
    private val ioDispatcher: CoroutineDispatcher
) : PollsRepository {

    private val polls = hashMapOf<Long, List<Poll>>()

    override suspend fun getPackUnvotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.votedOptionId == null }

    override suspend fun getPackVotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.votedOptionId != null }

    private suspend fun getPackPolls(packId: Long): List<Poll> = withContext(ioDispatcher) {
        val newPolls = polls[packId] ?: pollsApi
            .getPackPolls(packId)
            .map(::mapFromJson)

        polls[packId] = newPolls
        newPolls
    }

    override suspend fun vote(pollId: Long, optionId: Long) = withContext(ioDispatcher) {
        try {
            // Записывать результат в polls
            val request = VoteRequestJson(optionId)
            pollsApi.vote(pollId, request)
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            throw exception
        }
    }

    private fun mapFromJson(poll: PollJson) = Poll(
        id = poll.id,
        packId = poll.packId,
        options = poll.options.map { option ->
            PollOption(
                id = option.id,
                title = option.title,
                votesCount = option.votesCount
            )
        },
        votedOptionId = poll.vote?.optionId
    )

}