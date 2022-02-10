package pro.glideim.ui.chat.viewholder

import android.view.ViewGroup
import com.dengzii.adapter.AbsViewHolder
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.sdk.messages.GroupNotify

class GroupNotifyViewHolder(p: ViewGroup) : AbsViewHolder<GroupNotifyViewData>(p) {

    private val mTvContent by lazy { findViewById<MaterialTextView>(R.id.tv_content) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_chat_group_notify)
    }

    override fun onBindData(data: GroupNotifyViewData, position: Int) {

        val uid = data.notify.notify.data.uid
        val l = uid[0]
        mTvContent.text = when (data.notify.notify.type.toInt()) {
            GroupNotify.TYPE_MEMBER_ADDED -> "$l 已加入群聊"
            GroupNotify.TYPE_MEMBER_REMOVED -> "你已离开群聊"
            else -> "-"
        }
    }
}