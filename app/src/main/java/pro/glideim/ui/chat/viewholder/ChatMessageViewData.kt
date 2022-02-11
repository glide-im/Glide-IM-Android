package pro.glideim.ui.chat.viewholder

import pro.glideim.sdk.IMMessage

open class ChatMessageViewData(
    var showTitle: Boolean,
    var message: IMMessage,
    var unknown: Boolean = false
)