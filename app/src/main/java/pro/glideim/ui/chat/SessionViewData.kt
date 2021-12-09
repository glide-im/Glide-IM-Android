package pro.glideim.ui.chat

data class SessionViewData(
    var avatar: String = "",
    var title: String = "",
    var msg: String = "",
    var time: String = "",
    var unread: Int = 0
)