package pro.glideim.ui.chat

import pro.glideim.sdk.IMSession

class SessionViewData private constructor(
    val s: IMSession,
    val type: Int,
    val to: Long,
    val title: String,
    var updateAt: Long,
    val lastMsg: String,
    val avatar: String,
    val unread: Int,
    val preUpdateAt:Long
) {
    companion object {
        fun create(s: IMSession): SessionViewData {
            return SessionViewData(
                s = s,
                type = s.type,
                to = s.to,
                s.title,
                s.updateAt,
                s.lastMsg ?: "",
                s.avatar,
                s.unread,
                s.previousUpdateAt
            )
        }
    }

    override fun toString(): String {
        return "$type@$to"
    }
}