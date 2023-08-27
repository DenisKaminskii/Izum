package com.izum.data.repository

import androidx.annotation.WorkerThread
import com.izum.api.CreatePackRequestJson
import com.izum.api.CustomPacksApi
import com.izum.data.Pack
import javax.inject.Inject

interface CustomPacksRepository {

    @WorkerThread
    suspend fun getCustomPacks(): List<Pack.Custom>

    @WorkerThread
    suspend fun removePack(id: Long)

    @WorkerThread
    suspend fun createPack(title: String): Pair<Long, String>

}

class CustomPacksRepositoryImpl @Inject constructor(
    private val customPacksApi: CustomPacksApi
) : CustomPacksRepository {

    private val customPacks = mutableListOf<Pack.Custom>() // § SharedFlow

    override suspend fun getCustomPacks(): List<Pack.Custom> {
        val newPacks = customPacksApi.getPacks()
            .map(Pack.Custom::fromJson)

        customPacks.clear()
        customPacks.addAll(newPacks)
        return newPacks
    }

    override suspend fun removePack(id: Long) {
        customPacksApi.deletePack(id)
        customPacks.removeIf { pack -> pack.id == id }
    }

    override suspend fun createPack(title: String): Pair<Long, String> {
        val response = customPacksApi.createPack(
            CreatePackRequestJson(title)
        )

        return response.id to response.link
    }

}