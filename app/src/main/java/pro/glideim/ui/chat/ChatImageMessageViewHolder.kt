package pro.glideim.ui.chat

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.dengzii.adapter.AbsViewHolder
import com.dengzii.ktx.android.gone
import com.dengzii.ktx.android.hide
import com.dengzii.ktx.android.setImageTintColor
import com.dengzii.ktx.android.show
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.utils.loadImageClipCircle
import pro.glideim.utils.loadImageRoundCorners
import pro.glideim.utils.secToTimeSpan

class ChatImageMessageViewHolder(v: ViewGroup) : AbsViewHolder<ChatImageMessageViewData>(v) {

    private val mIvAvatarRight by lazy { findViewById<ImageView>(R.id.iv_avatar_right) }
    private val mTvTime by lazy { findViewById<MaterialTextView>(R.id.tv_time) }
    private val mIvMessage by lazy { findViewById<ImageView>(R.id.iv_image) }
    private val mMessageContainer by lazy { findViewById<ViewGroup>(R.id.ll_message_container) }
    private val mLlMessage by lazy { findViewById<LinearLayoutCompat>(R.id.fl_message) }
    private val mIvAvatarLeft by lazy { findViewById<ImageView>(R.id.iv_avatar_left) }
    private val mIvSendFailed by lazy { findViewById<ImageView>(R.id.iv_send_failed) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_chat_image_message)
    }

    override fun onBindData(data: ChatImageMessageViewData, position: Int) {
        mIvSendFailed.gone()

        val msg = data.message
        if (msg.isMe) {
            when {
                msg.isSendFailed -> {
                    mIvSendFailed.show()
                }
                msg.isSending -> {
                    //mIvSendFailed.show()
                    mIvSendFailed.setImageTintColor(R.color.gray)
                }
                msg.isSendSuccess -> {
                    //mIvSendFailed.show()
                    mIvSendFailed.setImageTintColor(R.color.primaryLightColor)
                }
            }
            mIvAvatarRight.loadImageClipCircle(msg.avatar)
            mIvAvatarRight.show()
            mIvAvatarLeft.hide()
            (mLlMessage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END
            if (!data.showTitle) {
                mIvAvatarRight.hide()
            } else {
                mIvAvatarRight.show()
            }
        } else {
            mIvAvatarLeft.loadImageClipCircle(msg.avatar)
            mIvAvatarLeft.show()
            mIvAvatarRight.hide()
            (mLlMessage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
            if (!data.showTitle) {
                mIvAvatarLeft.hide()
            } else {
                mIvAvatarLeft.show()
            }
        }
        mTvTime.text = msg.sendAt.secToTimeSpan()
        mIvMessage.loadImageRoundCorners(data.message.content, 4f)
        mMessageContainer.setOnLongClickListener {
            onLongClick(it, msg)
            return@setOnLongClickListener true
        }
    }
}