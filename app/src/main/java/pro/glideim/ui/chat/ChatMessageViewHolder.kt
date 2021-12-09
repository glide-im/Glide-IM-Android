package pro.glideim.ui.chat

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.ktx.android.hide
import com.dengzii.ktx.android.show
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R

class ChatMessageViewHolder(v: ViewGroup) : AbsViewHolder<MessageViewData>(v) {
    private val mIvAvatarRight by lazy { findViewById<ImageView>(R.id.iv_avatar_right) }
    private val mTvTime by lazy { findViewById<MaterialTextView>(R.id.tv_time) }
    private val mTvMsg by lazy { findViewById<MaterialTextView>(R.id.tv_msg) }
    private val mLlMessageContainer by lazy { findViewById<LinearLayoutCompat>(R.id.ll_message_container) }
    private val mCvMessageContainer by lazy { findViewById<CardView>(R.id.cv_message_container) }
    private val mIvAvatarLeft by lazy { findViewById<ImageView>(R.id.iv_avatar_left) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_chat_message)
    }

    override fun onBindData(data: MessageViewData, position: Int) {
        if (data.fromMe) {
            mIvAvatarRight.show()
            mIvAvatarLeft.hide()
            (mCvMessageContainer.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END
        } else {
            mIvAvatarLeft.show()
            mIvAvatarRight.hide()
            (mCvMessageContainer.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
        }
        mLlMessageContainer.orientation =
            if (mTvMsg.width > (mCvMessageContainer.parent as ViewGroup).measuredWidth - 60) {
                LinearLayoutCompat.VERTICAL
            } else {
                LinearLayoutCompat.HORIZONTAL
            }
        mTvTime.text = data.time
        mTvMsg.text = data.content
    }
}