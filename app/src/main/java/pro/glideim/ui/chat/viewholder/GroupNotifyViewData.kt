package pro.glideim.ui.chat.viewholder

import pro.glideim.sdk.IMGroupNotifyMessage

data class GroupNotifyViewData(
    val notify: IMGroupNotifyMessage
) : ChatMessageViewData(false, notify, false)