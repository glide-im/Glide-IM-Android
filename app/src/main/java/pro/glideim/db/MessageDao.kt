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

    @Query("SELECT COUNT(*) FROM message WHERE mid=:mid")
    suspend fun exist(mid: Long): Int

    @Query("SELECT * FROM Message WHERE uid=:uid AND targetId=:target AND targetType=:targetType")
    fun get(uid: Long, targetType: Int, target: Long): List<Message>
}