package com.polleo.data.repository

import androidx.annotation.WorkerThread
import com.polleo.api.custom.CustomPackAddPollRequestJson
import com.polleo.api.custom.CustomPackApi
import com.polleo.api.TitleJson
import com.polleo.data.EditPoll
import com.polleo.data.Pack
import com.polleo.data.Poll
import com.polleo.data.PollOption
import com.polleo.data.PollStatistic
import com.polleo.data.PollStatisticCategory
import com.polleo.data.PollStatisticSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

interface CustomPacksRepository {

    val packs: SharedFlow<List<Pack.Custom>>

    @WorkerThread
    suspend fun fetch()

    @WorkerThread
    suspend fun getPolls(packId: Long): Flow<List<Poll>>

    @WorkerThread
    suspend fun removePack(id: Long)

    @WorkerThread
    suspend fun createPack(title: String): Pair<Long, String>

    @WorkerThread
    suspend fun updatePack(id: Long, title: String)

    @WorkerThread
    suspend fun addPoll(packId: Long, edit: EditPoll)

    @WorkerThread
    suspend fun removePoll(packId: Long, pollId: Long)

    @WorkerThread
    suspend fun getPollStatistic(pollId: Long): PollStatistic

}

class CustomPacksRepositoryImpl @Inject constructor(
    private val customPacksApi: CustomPackApi
) : CustomPacksRepository {

    private val _packs = MutableSharedFlow<List<Pack.Custom>>(replay = 1)
    override val packs: SharedFlow<List<Pack.Custom>>
        get() = _packs

    private val polls: HashMap<Long, MutableSharedFlow<List<Poll>>> = hashMapOf()

    override suspend fun fetch() {
        val newPacks = customPacksApi.getPacks()
            .map(Pack.Custom::fromJson)

        _packs.emit(newPacks)
    }

    override suspend fun getPolls(packId: Long): Flow<List<Poll>> {
        if (!polls.containsKey(packId)) {
            polls[packId] = MutableSharedFlow(replay = 1)
        }

        val pollsFlow = polls[packId]!!
        val newPolls = customPacksApi.getCustomPackPolls(packId)
            .map(Poll::fromJson)

        pollsFlow.emit(newPolls)
        return pollsFlow
    }

    override suspend fun removePack(id: Long) {
        customPacksApi.deletePack(id)
        val current = _packs.replayCache.first()
        _packs.emit(current.filter { it.id != id })
    }

    override suspend fun createPack(title: String): Pair<Long, String> {
        val response = customPacksApi.createPack(TitleJson(title))
        return response.id to response.link
    }

    override suspend fun updatePack(id: Long, title: String) {
        val updatedPack = Pack.Custom.fromJson(
            json = customPacksApi.updatePack(id, TitleJson(title))
        )

        val current = _packs.replayCache.first()
        _packs.emit(current.map { if (it.id == id) updatedPack else it })
    }

    override suspend fun addPoll(packId: Long, edit: EditPoll){
        val addedPoll = customPacksApi.addPoll(
            id = packId,
            request = CustomPackAddPollRequestJson(
                options = listOf(
                    TitleJson(edit.topText),
                    TitleJson(edit.bottomText)
                )
            )
        ).let(Poll::fromJson)

        if (!polls.containsKey(packId)) {
            polls[packId] = MutableSharedFlow(replay = 1)
        }

        val pollsFlow = polls[packId]!!
        val currentPolls = pollsFlow.replayCache.first()
        this.polls[packId]?.emit(currentPolls + addedPoll)
    }

    override suspend fun removePoll(packId: Long, pollId: Long) {
        customPacksApi.removePoll(pollId)

        val pollsFlow = polls[packId]!!
        val currentPolls = pollsFlow.replayCache.first()
        val updatedPolls = currentPolls.filter { it.id != pollId }
        this.polls[packId]?.emit(updatedPolls)
    }

    override suspend fun getPollStatistic(pollId: Long): PollStatistic {
        val poll = customPacksApi.getPollStatistic(pollId)
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

}