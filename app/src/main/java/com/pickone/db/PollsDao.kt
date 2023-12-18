package com.pickone.db

import androidx.room.Dao
import androidx.room.Insert
import com.pickone.data.Poll

@Dao
interface PollsDao {

    @Insert
    fun insertAll(vararg poll: Poll)

}