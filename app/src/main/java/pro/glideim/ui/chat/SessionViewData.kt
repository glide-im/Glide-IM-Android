package pro.glideim.ui.chat

import pro.glideim.sdk.api.msg.SessionBean

data class SessionViewData(
    var avatar: String = "",
    var title: String = "",
    var msg: String = "",
    var time: String = "",
    var unread: Int = 0,
    var session: SessionBean
)