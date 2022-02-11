package pro.glideim.ui.chat.viewholder

import pro.glideim.sdk.IMMessage

data class NotifyViewData(
    val msg: IMMessage,
    val content: String
) : ChatMessageViewData(false, msg, false)