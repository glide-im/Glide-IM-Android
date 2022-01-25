package pro.glideim.ui.session

import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.ktx.android.antiShakeClick
import com.dengzii.ktx.android.hide
import com.dengzii.ktx.android.show
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.ui.chat.ChatActivity
import pro.glideim.utils.loadImageRoundCorners
import pro.glideim.utils.secToTimeSpan

class SessionViewHolder(v: ViewGroup) : AbsViewHolder<SessionViewData>(v) {

    private val mTvNewMessage by lazy { findViewById<TextView>(R.id.tv_new_message) }
    private val mTvTime by lazy { findViewById<MaterialTextView>(R.id.tv_time) }
    private val mTvContent by lazy { findViewById<MaterialTextView>(R.id.tv_content) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val mIvAvatar by lazy { findViewById<AppCompatImageView>(R.id.iv_avatar) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_session)
    }

    override fun onBindData(data: SessionViewData, position: Int) {
        mIvAvatar.loadImageRoundCorners(data.avatar, 6f)
        mTvTitle.text = data.title + "${data.to}"
        mTvContent.text = data.lastMsg
        if (data.unread > 0) {
            mTvNewMessage.show()
            mTvNewMessage.text = data.unread.toString()
        } else {
            mTvNewMessage.hide()
        }
        mTvTime.text = data.updateAt.secToTimeSpan()

        itemView.antiShakeClick {
            ChatActivity.start(context, data.to, data.type)
        }
    }

    override fun onBindData(data: SessionViewData, position: Int, payloads: MutableList<Any>?) {
        super.onBindData(data, position, payloads)

    }

    override fun getChangePayloads(old: Any, new_: Any): Any? {
        return super.getChangePayloads(old, new_)
    }
}