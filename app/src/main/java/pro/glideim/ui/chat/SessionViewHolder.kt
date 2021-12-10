package pro.glideim.ui.chat

import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.ktx.android.hide
import com.dengzii.ktx.android.show
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.loadImage

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
        mIvAvatar.loadImage(data.avatar)
        mTvTitle.text = data.title
        mTvContent.text = data.msg
        if (data.unread > 0) {
            mTvNewMessage.show()
            mTvNewMessage.text = data.unread.toString()
        } else {
            mTvNewMessage.hide()
        }
        mTvTime.text = data.time

        itemView.setOnClickListener {
            var id = data.session.uid2
            if (data.session.uid1 == GlideIM.getMyUID()) {
                id = data.session.uid1
            }
            ChatActivity.start(context, id)
        }
    }
}