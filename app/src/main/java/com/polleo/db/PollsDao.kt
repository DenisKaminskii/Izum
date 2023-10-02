package com.polleo.db

import androidx.room.Dao
import androidx.room.Insert
import com.polleo.data.Poll

@Dao
interface PollsDao {

    @Insert
    fun insertAll(vararg poll: Poll)

}