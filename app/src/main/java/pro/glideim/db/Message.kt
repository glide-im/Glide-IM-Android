package pro.glideim.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import pro.glideim.sdk.IMAccount
import pro.glideim.sdk.IMMessage

@Entity
data class Message(
    val uid: Long = 0,
    @PrimaryKey
    val mid: Long = 0,
    val cliSeq: Long = 0,
    val from: Long = 0,
    val to: Long = 0,
    val type: Int = 0,
    val sendAt: Long = 0,
    val createAt: Long = 0,
    val content: String = "",
    val targetId: Long = 0,
    val targetType: Int = 0,
    val state: Int = 0,
    val status: Int = 0,
    val recallBy:Long = 0,
    val seq: Long = 0
) {
    companion object {
        fun fromIMMessage(uid: Long, m: IMMessage): Message {
            return Message(
                uid = uid,
                mid = m.mid,
                cliSeq = m.cliSeq,
                from = m.from,
                to = m.to,
                type = m.type,
                sendAt = m.sendAt,
                createAt = m.createAt,
                content = m.content,
                targetId = m.targetId,
                targetType = m.targetType,
                state = m.state,
                status = m.status,
                recallBy = m.recallBy,
                seq = m.seq,
            )
        }
    }

    fun toIMMessage(account: IMAccount): IMMessage {
        return IMMessage(account).apply {
            mid = this@Message.mid
            cliSeq = this@Message.cliSeq
            from = this@Message.from
            to = this@Message.to
            type = this@Message.type
            sendAt = this@Message.sendAt
            createAt = this@Message.createAt
            content = this@Message.content
            targetId = this@Message.targetId
            targetType = this@Message.targetType
            state = this@Message.state
            status = this@Message.status
            seq = this@Message.seq
            recallBy = this@Message.recallBy
        }

    }
}