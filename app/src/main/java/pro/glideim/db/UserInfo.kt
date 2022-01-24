package pro.glideim.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import pro.glideim.sdk.api.user.UserInfoBean

@Entity
data class UserInfo(
    @PrimaryKey
    val uid: Long,
    val avatar: String,
    val nickname: String,
    val account: String,
    val updateAt: Long
) {
    companion object {
        fun fromUserInfoBean(u: UserInfoBean): UserInfo {
            return UserInfo(
                u.uid,
                u.avatar,
                u.nickname,
                u.account,
                updateAt = System.currentTimeMillis()
            )
        }
    }

    fun toUserInfoBean(): UserInfoBean {
        return UserInfoBean().apply {
            uid = this@UserInfo.uid
            avatar = this@UserInfo.avatar
            account = this@UserInfo.account
            nickname = this@UserInfo.nickname
        }
    }
}