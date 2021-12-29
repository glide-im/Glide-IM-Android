package pro.glideim

import android.app.Application
import android.content.Context
import pro.glideim.sdk.DataStorage
import pro.glideim.sdk.api.group.GroupInfoBean
import pro.glideim.sdk.api.user.UserInfoBean
import pro.glideim.sdk.entity.IMSession

class UserPerf : DataStorage {

    private var token = ""

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

    }

    override fun loadTempUserInfo(uid: Long): UserInfoBean? {
        return null
    }

    override fun storeTempGroupInfo(groupInfoBean: GroupInfoBean?) {

    }

    override fun loadTempGroupInfo(gid: Long): GroupInfoBean? {
        return null
    }

    override fun storeSession(uid: Long, session: IMSession?) {

    }

    override fun loadSessions(uid: Long): MutableList<IMSession> {
        return mutableListOf()
    }
}