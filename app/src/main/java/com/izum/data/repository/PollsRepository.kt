package com.izum.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.izum.api.PollsApi
import com.izum.api.VoteRequestJson
import com.izum.data.SendVoteException

interface PollsRepository {

    @WorkerThread
    @Throws(SendVoteException::class)
    suspend fun vote(pollId: Long, optionId: Long)

}

class PollsRepositoryImpl(
    private val pollsApi: PollsApi
) : PollsRepository {

    override suspend fun vote(pollId: Long, optionId: Long) {
        try {
            val request = VoteRequestJson(optionId)
            pollsApi.vote(pollId, request)
        } catch (exception: Exception) {
            Log.e("Steve", exception.toString())
            throw SendVoteException()
        }
    }

}