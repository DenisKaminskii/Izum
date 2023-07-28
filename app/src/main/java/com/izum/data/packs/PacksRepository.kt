package com.izum.data.packs

import com.izum.api.PackJson
import com.izum.api.PacksApi
import com.izum.api.PollJson
import com.izum.domain.core.MutableStateProducer
import com.izum.domain.core.StateProducer
import java.lang.Exception
import javax.inject.Inject

interface PacksRepository : StateProducer<PacksRepository.State> {

    sealed interface State {
        object NoData : State

        data class Packs(
            val general: List<Pack>,
            val custom: List<Pack>,
            val generalPacks: HashMap<Long, List<Poll>> = hashMapOf()
        ) : State
    }

    @Throws(Exception::class)
    suspend fun fetchPacks()

    @Throws(Exception::class)
    suspend fun fetchPackPolls(packId: Long)

}

class PacksRepositoryImpl @Inject constructor(
    private val packsApi: PacksApi
) : PacksRepository,
    MutableStateProducer<PacksRepository.State>(
        initialValue = PacksRepository.State.NoData
    ) {

    override suspend fun fetchPacks() {
        val general = packsApi.getPacks()
        val custom = packsApi.getPacks()

        updateState {
            PacksRepository.State.Packs(
                general = general.map(::mapFromJson),
                custom = custom.map(::mapFromJson)
            )
        }
    }

    override suspend fun fetchPackPolls(packId: Long) {
        if (state !is PacksRepository.State.Packs) return

        val packPolls = packsApi.getPackPolls(packId).map(::mapFromJson)
        val packs = state.generalPacks
        packs[packId] = packPolls

        updateState { currentState ->
            currentState.ifPacks { packsState ->
                packsState.copy(
                    generalPacks = packs
                )
            }
            // TODO: ifNoData
        }
    }

    private fun mapFromJson(packJson: PackJson) = Pack(
        id = packJson.id,
        title = packJson.title,
        description = packJson.description,
        isPaid = packJson.isPaid,
        productId = packJson.productId,
        pollsCount = packJson.pollsCount,
        authorId = packJson.author.id
    )

    private fun mapFromJson(poll: PollJson) = Poll(
        id = poll.id
    )

}

fun PacksRepository.State.ifNoData(func: (state: PacksRepository.State.NoData) -> Unit): PacksRepository.State {
    if (this is PacksRepository.State.NoData) {
        func.invoke(this)
    }
    return this
}

fun PacksRepository.State.ifPacks(func: (state: PacksRepository.State.Packs) -> Unit): PacksRepository.State {
    if (this is PacksRepository.State.Packs) {
        func.invoke(this)
    }
    return this
}