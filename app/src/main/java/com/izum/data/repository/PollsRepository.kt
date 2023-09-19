package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollApi
import com.izum.api.PollJson
import com.izum.api.SuggestPollRequestJson
import com.izum.api.VoteRequestJson
import com.izum.data.Author
import com.izum.data.EditPoll
import com.izum.data.Poll
import com.izum.data.PollOption
import com.izum.data.PollStatistic
import com.izum.data.PollStatisticCategory
import com.izum.data.PollStatisticSection
import com.izum.data.Vote
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

interface PollsRepository {

    @WorkerThread
    suspend fun getPackPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackUnvotedPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackVotedPolls(packId: Long): List<Poll>

    @WorkerThread
    @Throws(IOException::class)
    suspend fun vote(pollId: Long, optionId: Long)

    @WorkerThread
    suspend fun getPollStatistic(pollId: Long): PollStatistic

    @WorkerThread
    suspend fun suggestPoll(edit: EditPoll)

}

class PollsRepositoryImpl(
    private val pollsApi: PollApi
) : PollsRepository {

    private val polls = hashMapOf<Long, List<Poll>>()

    override suspend fun getPackPolls(packId: Long): List<Poll> {
        val newPolls = polls[packId] ?: pollsApi
            .getPackPolls(packId)
            .map(::mapFromJson)

        polls[packId] = newPolls
        return newPolls
    }

    override suspend fun getPackUnvotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.voted?.optionId == null }

    override suspend fun getPackVotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.voted?.optionId != null }

    override suspend fun vote(pollId: Long, optionId: Long) {
        try {
            // § Записывать результат в polls
            val request = VoteRequestJson(optionId)
            pollsApi.vote(pollId, request)
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            throw exception
        }
    }

    override suspend fun getPollStatistic(pollId: Long): PollStatistic {
        val poll = pollsApi.getPollStatistic(pollId)
        return PollStatistic(
            options = poll.options.map { option ->
                PollOption(
                    id = option.id,
                    title = option.title,
                    votesCount = 0 // § option.votesCount
                )
            },
            sections = poll.sections.map { section ->
                PollStatisticSection(
                    title = section.title,
                    categories = section.categories.map { category ->
                        PollStatisticCategory(
                            title = category.title,
                            options = category.options.map { option ->
                                PollOption(
                                    id = option.id,
                                    title = "",
                                    votesCount = 0 // § option.votesCount
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    override suspend fun suggestPoll(edit: EditPoll) {
        pollsApi.suggestPoll(SuggestPollRequestJson(
            options = listOf(edit.topText, edit.bottomText)
        ))
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
        author = poll.author?.let { author ->
            Author(author.id)
        },
        totalVotesCount = poll.totalVotesCount,
        voted = poll.voted?.let {
            Vote(
                optionId = it.optionId,
                date = it.date
            )
        },
        createdAt = poll.createdAt
    )

}