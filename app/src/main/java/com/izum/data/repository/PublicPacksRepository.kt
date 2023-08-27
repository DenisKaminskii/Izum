package com.izum.data.repository

import androidx.annotation.WorkerThread
import com.izum.api.PacksApi
import com.izum.data.Pack

interface PublicPacksRepository {

    @WorkerThread
    suspend fun getPacks(): List<Pack.Public>

}

class PublicPacksRepositoryImpl(
    private val packsApi: PacksApi
) : PublicPacksRepository {

    private val publicPacks = mutableListOf<Pack.Public>() // § SharedFlow

    override suspend fun getPacks(): List<Pack.Public> {
        val newPack = packsApi.getPacks()
            .map(Pack.Public::fromJson)

        publicPacks.clear()
        publicPacks.addAll(newPack)
        return newPack
    }

}
