package pro.glideim.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import pro.glideim.sdk.IMAccount
import pro.glideim.sdk.IMSession

@Entity
data class Session(
    @PrimaryKey
    val id: String = "",
    val uid: Long = 0,
    val to: Long = 0,
    val lastMsgSender: Long = 0,
    val title: String,
    val avatar: String? = null,
    val unread: Int = 0,
    val updateAt: Long = 0,
    val previousUpdateAt: Long = 0,
    val type: Int = 0,
    val lastMsg: String? = null,
    val lastMsgId: Long = 0,
    val createAt: Long = 0
) {
    companion object {
        fun fromIMSession(uid: Long, s: IMSession): Session {
            return Session(
                id = "$uid@${s.to}",
                uid = uid,
                to = s.to,
                lastMsgSender = s.lastMsgSender,
                title = s.title,
                avatar = s.avatar,
                unread = s.unread,
                updateAt = s.updateAt,
                previousUpdateAt = s.previousUpdateAt,
                type = s.type,
                lastMsg = s.lastMsg,
                lastMsgId = s.lastMsgId,
                createAt = s.createAt
            )
        }
    }

    fun toImSession(account: IMAccount): IMSession {
        return IMSession(account, to, type).apply {
            to = this@Session.to
            lastMsgSender = this@Session.lastMsgSender
            title = this@Session.title
            avatar = this@Session.avatar
            unread = this@Session.unread
            updateAt = this@Session.updateAt
            previousUpdateAt = this@Session.previousUpdateAt
            type = this@Session.type
            lastMsg = this@Session.lastMsg
            lastMsgId = this@Session.lastMsgId
            createAt = this@Session.createAt
        }
    }
}