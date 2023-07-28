package com.izum.data.packs

import androidx.annotation.WorkerThread
import com.izum.api.PackJson
import com.izum.api.PacksApi
import com.izum.api.PollJson
import com.izum.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PacksRepository {

    @WorkerThread
    suspend fun getPacks() : Flow<List<Pack>>

    @WorkerThread
    suspend fun getCustomPacks() : Flow<List<Pack>>

    @WorkerThread
    suspend fun getPackPolls(packId: Long) : Flow<List<Poll>>

}

class PacksRepositoryImpl @Inject constructor(
    private val packsApi: PacksApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PacksRepository {

    override suspend fun getPacks(): Flow<List<Pack>> {
        return flowOf(
            packsApi.getPacks()
                .map(::mapFromJson)
        ).flowOn(ioDispatcher)
    }

    override suspend fun getCustomPacks(): Flow<List<Pack>> {
        return flowOf(
            packsApi.getPacks() // TODO: replace on custom packs
                .map(::mapFromJson)
        ).flowOn(ioDispatcher)
    }

    override suspend fun getPackPolls(packId: Long): Flow<List<Poll>> {
        return flowOf(
            packsApi.getPackPolls(packId)
                .map(::mapFromJson)
        ).flowOn(ioDispatcher)
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
