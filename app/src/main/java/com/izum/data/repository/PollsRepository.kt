package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollsApi
import com.izum.api.VoteRequestJson
import com.izum.data.SendVoteException
import com.izum.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface PollsRepository {

    @WorkerThread
    @Throws(SendVoteException::class)
    suspend fun vote(pollId: Long, optionId: Long)

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
            throw SendVoteException()
        }
    }

}