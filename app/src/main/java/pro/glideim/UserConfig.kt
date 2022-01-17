package pro.glideim

import android.app.Application
import android.content.Context
import pro.glideim.sdk.DataStorage
import pro.glideim.sdk.DefaultDataStoreImpl
import pro.glideim.sdk.IMSession
import pro.glideim.sdk.api.group.GroupInfoBean
import pro.glideim.sdk.api.user.UserInfoBean

class UserConfig : DataStorage {

    private var token = ""
    private var uid = 0L
    private val d = DefaultDataStoreImpl()

    companion object {
        private val instance = UserConfig()
        private lateinit var application: Application

        fun init(app: Application) {
            val sp = app.getSharedPreferences("u", Context.MODE_PRIVATE)
            instance.token = sp.getString("token", "") ?: ""
            instance.uid = sp.getLong("d_uid", 0)
            application = app
        }

        fun logout() {
            instance.storeToken(instance.uid, "")
        }

        fun getInstance(): UserConfig {
            return instance
        }
    }

    override fun getDefaultAccountUid(): Long {
        return uid
    }

    override fun storeToken(uid: Long, token: String?) {
        this.token = token ?: ""
        val sp = application.getSharedPreferences("u", Context.MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("token", token)
        edit.putLong("d_uid", uid)
        edit.apply()
    }

    override fun loadToken(uid: Long): String {
        return token
    }

    override fun storeTempUserInfo(userInfoBean: UserInfoBean?) {
        d.storeTempUserInfo(userInfoBean)
    }

    override fun loadTempUserInfo(uid: Long): UserInfoBean? {
        return d.loadTempUserInfo(uid)
    }

    override fun storeTempGroupInfo(groupInfoBean: GroupInfoBean?) {
        d.storeTempGroupInfo(groupInfoBean)
    }

    override fun loadTempGroupInfo(gid: Long): GroupInfoBean? {
        return d.loadTempGroupInfo(gid)
    }

    override fun storeSession(uid: Long, session: IMSession?) {
        d.storeSession(uid, session)
    }

    override fun loadSessions(uid: Long): MutableList<IMSession> {
        return d.loadSessions(uid)
    }
}