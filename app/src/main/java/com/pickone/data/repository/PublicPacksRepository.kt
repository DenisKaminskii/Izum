package com.pickone.data.repository

import androidx.annotation.WorkerThread
import com.pickone.api.PackApi
import com.pickone.api.PollApi
import com.pickone.api.PollJson
import com.pickone.api.SuggestPollRequestJson
import com.pickone.api.VoteRequestJson
import com.pickone.data.Author
import com.pickone.data.EditPoll
import com.pickone.data.Pack
import com.pickone.data.Poll
import com.pickone.data.PollOption
import com.pickone.data.PollStatistic
import com.pickone.data.PollStatisticCategory
import com.pickone.data.PollStatisticSection
import com.pickone.data.Vote
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import java.io.IOException

interface PublicPacksRepository {

    val packs: SharedFlow<List<Pack.Public>>

    @WorkerThread
    suspend fun fetchFeed()

    @WorkerThread
    suspend fun getPackPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackUnvotedPolls(packId: Long): List<Poll>

    @WorkerThread
    suspend fun getPackVotedPolls(packId: Long): List<Poll>

    @WorkerThread
    @Throws(IOException::class)
    suspend fun vote(pollId: Long, optionId: Long, elapsedTimeMs: Int)

    @WorkerThread
    suspend fun getPollStatistic(pollId: Long): PollStatistic

    @WorkerThread
    suspend fun suggestPoll(edit: EditPoll)

}

class PublicPacksRepositoryImpl(
    private val packsApi: PackApi,
    private val pollsApi: PollApi
) : PublicPacksRepository {

    private val _packs = MutableSharedFlow<List<Pack.Public>>(replay = 1)
    override val packs: SharedFlow<List<Pack.Public>>
        get() = _packs

    private val polls = hashMapOf<Long, List<Poll>>()

    override suspend fun fetchFeed() {
        try {
            val newPacks = packsApi.getPacks()
                .map(Pack.Public::fromJson)
                .sortedBy { it.id }
                .sortedBy { it.isPaid }

            _packs.emit(newPacks)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed fo fetch feed packs")
        }
    }

    override suspend fun getPackPolls(packId: Long): List<Poll> {
       try {
           val newPolls = pollsApi
               .getPackPolls(packId)
               .map(::mapFromJson)

           polls[packId] = newPolls
           return newPolls
       } catch (exception: Exception) {
           Timber.e(exception, "Failed fo fetch pack polls")
           return emptyList()
       }
    }

    override suspend fun getPackUnvotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.voted?.optionId == null }

    override suspend fun getPackVotedPolls(packId: Long): List<Poll> =
        getPackPolls(packId)
            .filter { poll -> poll.voted?.optionId != null }

    override suspend fun vote(pollId: Long, optionId: Long, elapsedTimeMs: Int) {
        try {
            val request = VoteRequestJson(optionId, elapsedTimeMs)
            pollsApi.vote(pollId, request)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to vote")
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
                    votesCount = option.votesCount
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
                                    votesCount = option.votesCount
                                )
                            }
                        )
                    }
                )
            }
        )
    }

    override suspend fun suggestPoll(edit: EditPoll) {
        pollsApi.suggestPoll(
            SuggestPollRequestJson(
            options = listOf(edit.topText, edit.bottomText)
        )
        )
    }

    private fun mapFromJson(poll: PollJson) = Poll(
        id = poll.id,
        packId = poll.packId,
        options = poll.options.map { option ->
            PollOption(
                id = option.id,
                title = option.title,
                imageUrl = option.imageUrl,
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

    inline fun <reified T> Moshi.listAdapter(): JsonAdapter<List<T>> =
        adapter(Types.newParameterizedType(List::class.java, T::class.java))

    inline fun <reified T> Moshi.parseList(json: String?): List<T?>? =
        try {
            listAdapter<T>().fromJson(json ?: "")
        } catch (_: Throwable) {
            null
        }

}
