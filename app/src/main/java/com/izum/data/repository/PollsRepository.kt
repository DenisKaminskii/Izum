package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollsApi
import com.izum.api.VoteRequestJson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

interface PollsRepository {

    @WorkerThread
    @Throws(IOException::class)
    suspend fun vote(pollId: Long, optionId: Long)

    @WorkerThread
    @Throws(Exception::class)
    suspend fun suggest()

}

class PollsRepositoryImpl(
    private val pollsApi: PollsApi,
    private val ioDispatcher: CoroutineDispatcher
) : PollsRepository {

    override suspend fun vote(pollId: Long, optionId: Long) = withContext(ioDispatcher) {
        try {
            val request = VoteRequestJson(optionId)
            pollsApi.vote(pollId, request)
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            throw exception
        }
    }

    override suspend fun suggest() {
        // TODO:
    }

}