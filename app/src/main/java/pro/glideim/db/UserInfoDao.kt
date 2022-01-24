package pro.glideim.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserInfoDao {

    @Insert
    suspend fun add(vararg s: UserInfo)

    @Update
    suspend fun update(vararg s: UserInfo)

    @Query("SELECT COUNT(*) FROM userinfo WHERE uid=:uid")
    suspend fun exist(uid: Long): Int

    @Query("SELECT * FROM userinfo WHERE uid=:uid")
    fun get(uid: Long): UserInfo?
}