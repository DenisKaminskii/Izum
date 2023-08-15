package com.izum.data.repository

import androidx.annotation.WorkerThread
import com.izum.api.PackJson
import com.izum.api.PacksApi
import com.izum.data.Pack
import com.izum.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

interface PacksRepository {

    @WorkerThread
    suspend fun getPacks(): Flow<List<Pack>>

    @WorkerThread
    suspend fun getCustomPacks(): Flow<List<Pack>>

}

class PacksRepositoryImpl(
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

    private fun mapFromJson(packJson: PackJson) = Pack(
        id = packJson.id,
        title = packJson.title,
        description = packJson.description,
        isPaid = packJson.isPaid,
        productId = packJson.productId,
        pollsCount = packJson.pollsCount,
        authorId = packJson.author?.id
    )

}
