package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollApi
import com.izum.api.PollJson
import com.izum.api.VoteRequestJson
import com.izum.data.Poll
import com.izum.data.PollOption
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.IOException

interface PollsRepository {

    @WorkerThread
    suspend fun getPackPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackVotedPolls(packId: Long): List<Poll>

    @WorkerThread
    @Throws(IOException::class)
    suspend fun vote(pollId: Long, optionId: Long)

}

class PollsRepositoryImpl(
    private val pollsApi: PollApi, private val ioDispatcher: CoroutineDispatcher
) : PollsRepository {

    private val polls = hashMapOf<Long, List<Poll>>()

    override suspend fun getPackPolls(packId: Long): List<Poll> = withContext(ioDispatcher) {
        if (polls.containsKey(packId)) {
            polls[packId]!!
        } else {
            val newPolls = pollsApi.getPackPolls(packId).map(::mapFromJson)
            polls[packId] = newPolls
            newPolls
        }
    }

    override suspend fun getPackVotedPolls(packId: Long): List<Poll> {
        return getPackPolls(packId)
            .filter { poll -> poll.votedOptionId != null }
    }

    override suspend fun vote(pollId: Long, optionId: Long) = withContext(ioDispatcher) {
        try {
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