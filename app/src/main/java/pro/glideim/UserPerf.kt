package pro.glideim

import android.content.Context
import pro.glideim.sdk.DataStorage
import pro.glideim.sdk.api.user.UserInfoBean

class UserPerf : DataStorage {

    companion object {
        private val instance = UserPerf()

        fun init(context: Context) {

        }

        fun getInstance(): UserPerf {
            return instance
        }
    }

    override fun storeToken(token: String?) {

    }

    override fun storeTempUserInfo(userInfoBean: UserInfoBean?) {

    }

    override fun loadToken(): String {
        return "VWC3TJj3YcG6QrK6V1IKJ1sP0klsMj7i"
    }

    override fun loadTempUserInfo(): List<UserInfoBean> {
        return emptyList()
    }

}