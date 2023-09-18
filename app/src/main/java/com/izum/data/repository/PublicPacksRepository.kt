package com.izum.data.repository

import androidx.annotation.WorkerThread
import com.izum.api.PackApi
import com.izum.data.Pack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface PublicPacksRepository {

    val packs: SharedFlow<List<Pack.Public>>

    @WorkerThread
    suspend fun fetch()

}

class PublicPacksRepositoryImpl(
    private val packsApi: PackApi
) : PublicPacksRepository {

    private val _packs = MutableSharedFlow<List<Pack.Public>>(replay = 1)
    override val packs: SharedFlow<List<Pack.Public>>
        get() = _packs

    override suspend fun fetch() {
        val newPacks = packsApi.getPacks()
            .map(Pack.Public::fromJson)

        _packs.emit(newPacks)
    }

}
