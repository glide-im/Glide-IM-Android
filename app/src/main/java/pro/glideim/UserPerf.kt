package pro.glideim

import android.app.Application
import android.content.Context
import pro.glideim.sdk.DataStorage
import pro.glideim.sdk.DefaultDataStoreImpl
import pro.glideim.sdk.api.group.GroupInfoBean
import pro.glideim.sdk.api.user.UserInfoBean
import pro.glideim.sdk.IMSession

class UserPerf : DataStorage {

    private var token = ""
    private val d = DefaultDataStoreImpl()

    companion object {
        private val instance = UserPerf()
        private lateinit var application: Application

        fun init(app: Application) {
            val sp = app.getSharedPreferences("u", Context.MODE_PRIVATE)
            instance.token = sp.getString("token", "") ?: ""
            application = app
        }

        fun logout() {
            instance.storeToken(0, "")
        }

        fun getInstance(): UserPerf {
            return instance
        }
    }

    override fun storeToken(uid: Long, token: String?) {
        this.token = token ?: ""
        val sp = application.getSharedPreferences("u", Context.MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("token", token)
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