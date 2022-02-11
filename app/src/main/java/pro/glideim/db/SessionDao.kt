package pro.glideim.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SessionDao {

    @Insert
    suspend fun add(vararg s: Session)

    @Update
    suspend fun update(vararg s: Session)

    @Query("SELECT * FROM session WHERE id=:id LIMIT 1")
    suspend fun exist(id: String): List<Session>

    @Query("SELECT * FROM session WHERE uid=:uid")
    fun get(uid: Long): List<Session>
}