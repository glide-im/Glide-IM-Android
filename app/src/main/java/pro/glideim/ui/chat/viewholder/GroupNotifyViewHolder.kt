package pro.glideim.ui.chat.viewholder

import android.view.ViewGroup
import com.dengzii.adapter.AbsViewHolder
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R

class GroupNotifyViewHolder(p: ViewGroup) : AbsViewHolder<NotifyViewData>(p) {

    private val mTvContent by lazy { findViewById<MaterialTextView>(R.id.tv_content) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_chat_group_notify)
    }

    override fun onBindData(data: NotifyViewData, position: Int) {
        mTvContent.text = data.content
    }
}