package pro.glideim.ui.chat

import pro.glideim.sdk.IMMessage

open class ChatMessageViewData(
    var showTitle: Boolean,
    val message: IMMessage,
    var unknown: Boolean = false
)