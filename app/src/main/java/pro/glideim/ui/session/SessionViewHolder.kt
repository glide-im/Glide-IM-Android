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
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.Constants
import pro.glideim.ui.chat.ChatActivity
import pro.glideim.utils.GroupAvatarUtils
import pro.glideim.utils.loadImageRoundCorners
import pro.glideim.utils.secToTimeSpan

class SessionViewHolder(v: ViewGroup) : AbsViewHolder<SessionViewData>(v) {

    private val mTvNewMessage by lazy { findViewById<TextView>(R.id.tv_new_message) }
    private val mTvTime by lazy { findViewById<MaterialTextView>(R.id.tv_time) }
    private val mTvContent by lazy { findViewById<MaterialTextView>(R.id.tv_content) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val mIvAvatar by lazy { findViewById<AppCompatImageView>(R.id.iv_avatar) }
    private val mVgContainer by lazy { findViewById<ViewGroup>(R.id.vg_container) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_session)
    }

    override fun onBindData(data: SessionViewData, position: Int) {

        if (data.type == Constants.SESSION_TYPE_GROUP) {
            GroupAvatarUtils.loadAvatar((context as BaseActivity).account, data.to, 4f, mIvAvatar)
        } else {
            mIvAvatar.loadImageRoundCorners(data.avatar, 4f)
        }

        mTvTitle.text = data.title + "${data.to}"
//        mTvContent.text = data.lastMsg
        mTvContent.text = data.s.lastMsg
        if (data.unread > 0) {
            mTvNewMessage.show()
            mTvNewMessage.text = data.unread.toString()
        } else {
            mTvNewMessage.hide()
        }
        mTvTime.text = data.updateAt.secToTimeSpan()

        mVgContainer.antiShakeClick {
            ChatActivity.start(context, data.to, data.type)
        }
    }

    override fun onBindData(data: SessionViewData, position: Int, payloads: MutableList<Any>?) {
        super.onBindData(data, position, payloads)

    }

}