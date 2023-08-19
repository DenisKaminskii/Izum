package com.izum.data.repository

import android.graphics.Color
import androidx.annotation.WorkerThread
import com.izum.api.PackJson
import com.izum.api.PacksApi
import com.izum.data.Pack
import com.izum.data.PackColors
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

    var counter = 0

    private fun mapFromJson(packJson: PackJson) : Pack {
        val pack = Pack(
            id = packJson.id,
            title = packJson.title,
            description = packJson.description,
            isPaid = packJson.isPaid,
            productId = packJson.productId,
            pollsCount = packJson.pollsCount,
            authorId = packJson.author?.id,
            colors = colors[counter]
        )
        counter++
        return pack
    }

}

val colors = listOf<PackColors>(
    PackColors(
        contentColor = Color.parseColor("#FFFFFF"),
        gradientStartColor = Color.parseColor("#4D5297"),
        gradientEndColor = Color.parseColor("#49A0FA"),
    ),
    PackColors(
        contentColor = Color.parseColor("#FFFFFF"),
        gradientStartColor = Color.parseColor("#5C27FE"),
        gradientEndColor = Color.parseColor("#C165DD"),
    ),
    PackColors(
        contentColor = Color.parseColor("#343434"),
        gradientStartColor = Color.parseColor("#FF7B02"),
        gradientEndColor = Color.parseColor("#FFCB52"),
    ),
    PackColors(
        contentColor = Color.parseColor("#FFFFFF"),
        gradientStartColor = Color.parseColor("#1B1B1B"),
        gradientEndColor = Color.parseColor("#353535"),
    )
)
