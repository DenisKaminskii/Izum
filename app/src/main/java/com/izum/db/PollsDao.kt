package com.izum.db

import androidx.room.Dao
import androidx.room.Insert
import com.izum.data.Poll

@Dao
interface PollsDao {

    @Insert
    fun insertAll(vararg poll: Poll)

}