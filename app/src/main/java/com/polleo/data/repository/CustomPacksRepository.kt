package com.polleo.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.polleo.api.custom.CustomPackAddPollRequestJson
import com.polleo.api.custom.CustomPackApi
import com.polleo.api.TitleJson
import com.polleo.api.custom.toModel
import com.polleo.data.EditPoll
import com.polleo.data.Pack
import com.polleo.data.Poll
import com.polleo.data.PollOption
import com.polleo.data.PollStatistic
import com.polleo.data.PollStatisticCategory
import com.polleo.data.PollStatisticSection
import com.polleo.data.retrieveCodeData
import com.polleo.domain.core.PreferenceCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.lang.Exception
import javax.inject.Inject

data class CustomPacksState(
    val myPacks: List<Pack.Custom> = emptyList(),
    val addedPacks: List<Pack.Custom> = emptyList()
)

interface CustomPacksRepository {

    val packs: SharedFlow<CustomPacksState>

    @WorkerThread
    suspend fun fetchMyPacks()

    @WorkerThread
    suspend fun fetchAddedPacks()

    @WorkerThread
    suspend fun getPolls(packId: Long, packToken: String): Flow<List<Poll>>

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

    @WorkerThread
    suspend fun getCustomPack(packId: Long, token: String): Pack.Custom?

}

class CustomPacksRepositoryImpl @Inject constructor(
    private val customPacksApi: CustomPackApi,
    private val preferenceCache: PreferenceCache
) : CustomPacksRepository {

    companion object {
        private const val KEY_ADDED_CUSTOM_PACKS = "KEY_ADDED_CUSTOM_PACKS"
    }

    private val _packs = MutableStateFlow(CustomPacksState())
    override val packs: StateFlow<CustomPacksState>
        get() = _packs

    private val polls: HashMap<Long, MutableSharedFlow<List<Poll>>> = hashMapOf()

    private val addedPacks: List<AddedCustomPack>
        get() = preferenceCache.getList(KEY_ADDED_CUSTOM_PACKS, AddedCustomPack::class.java).orEmpty()

    override suspend fun fetchMyPacks() {
        val myPacks = customPacksApi.getMyPacks()
            .map { it.toModel(isMine = true) }

        _packs.update { state ->
            state.copy(myPacks = myPacks,)
        }
    }

    override suspend fun fetchAddedPacks() {
        val addedPacks = addedPacks
            .mapNotNull { pack ->
                try {
                    customPacksApi.getCustomPack(pack.id, pack.token)
                        .toModel(isMine = false)
                } catch (e: Exception) {
                    null
                }
            }

        _packs.update { state ->
            state.copy(addedPacks = addedPacks)
        }
    }

    override suspend fun getPolls(packId: Long, packToken: String): Flow<List<Poll>> {
        if (!polls.containsKey(packId)) {
            polls[packId] = MutableSharedFlow(replay = 1)
        }

        val pollsFlow = polls[packId]!!
        val newPolls = customPacksApi.getCustomPackPolls(packId, packToken)
            .map(Poll::fromJson)

        pollsFlow.emit(newPolls)
        return pollsFlow
    }

    override suspend fun removePack(id: Long) {
        customPacksApi.deletePack(id)

        _packs.update { state ->
            state.copy(
                myPacks = state.myPacks.filter { it.id != id }
            )
        }
    }

    override suspend fun createPack(title: String): Pair<Long, String> {
        val response = customPacksApi.createPack(TitleJson(title))
        return response.id to response.code
    }

    override suspend fun updatePack(id: Long, title: String) {
        val updatedPack = customPacksApi.updatePack(id, TitleJson(title)).toModel(isMine = true)

        _packs.update { state ->
            state.copy(
                myPacks = state.myPacks.map { if (it.id == id) updatedPack else it }
            )
        }
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

    override suspend fun getCustomPack(packId: Long, token: String): Pack.Custom? {
        return try {
            val pack = customPacksApi.getCustomPack(packId, token = token)
            val customPack = pack.toModel(isMine = false)
            addNewCustomPack(customPack)
            customPack
        } catch (e: Exception) {
            Log.d("Steve", "Error while fetching custom pack", e)
            null
        }
    }

    private fun addNewCustomPack(pack: Pack.Custom) {
        if (addedPacks.any { it.id == pack.id }) return
        val codeData = pack.code.retrieveCodeData()

        if (codeData == null) {
            Log.e("Steve", "Error while parsing code data")
            return
        }

        preferenceCache.putList(
            KEY_ADDED_CUSTOM_PACKS,
            addedPacks + listOf(AddedCustomPack(id = codeData.first, token = codeData.second)),
            AddedCustomPack::class.java
        )

        _packs.update { state ->
            state.copy(
                addedPacks = state.addedPacks + listOf(pack)
            )
        }
    }

}