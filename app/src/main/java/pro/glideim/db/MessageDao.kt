package pro.glideim.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageDao {

    @Insert
    fun add(vararg message: Message)

    @Update
    suspend fun update(vararg s: Message)

    @Query("SELECT * FROM message WHERE mid=:id LIMIT 1")
    suspend fun exist(id: Long): List<Message>

    @Query("SELECT * FROM Message WHERE uid=:uid AND targetId=:target AND targetType=:targetType")
    fun get(uid: Long, targetType: Int, target: Long): List<Message>
}