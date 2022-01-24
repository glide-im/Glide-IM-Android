package pro.glideim.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Session::class, Message::class, UserInfo::class], version = 2, exportSchema = false)
abstract class GlideIMDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun userInfoDao(): UserInfoDao

    companion object {
        fun getDb(context: Context): GlideIMDatabase {
            return Room.databaseBuilder(context, GlideIMDatabase::class.java, "glide-im-db")
                .build()
        }
    }
}