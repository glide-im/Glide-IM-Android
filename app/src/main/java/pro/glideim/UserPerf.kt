package pro.glideim

import android.app.Application
import android.content.Context
import pro.glideim.sdk.DataStorage
import pro.glideim.sdk.api.user.UserInfoBean

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
            instance.storeToken("")
        }

        fun getInstance(): UserPerf {
            return instance
        }
    }

    override fun storeToken(token: String?) {
        this.token = token ?: ""
        val sp = application.getSharedPreferences("u", Context.MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("token", token)
        edit.apply()
    }

    override fun storeTempUserInfo(userInfoBean: UserInfoBean?) {

    }

    override fun loadToken(): String {
        return token
    }

    override fun loadTempUserInfo(): List<UserInfoBean> {
        return emptyList()
    }

}