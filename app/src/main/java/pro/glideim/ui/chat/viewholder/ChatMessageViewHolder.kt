package pro.glideim.ui.chat.viewholder

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
import pro.glideim.utils.secToTimeSpan

class ChatMessageViewHolder(v: ViewGroup) : AbsViewHolder<ChatMessageViewData>(v) {

    private val mIvAvatarRight by lazy { findViewById<ImageView>(R.id.iv_avatar_right) }
    private val mTvTime by lazy { findViewById<MaterialTextView>(R.id.tv_time) }
    private val mTvMsg by lazy { findViewById<MaterialTextView>(R.id.tv_msg) }
    private val mLlMessageContainer by lazy { findViewById<LinearLayoutCompat>(R.id.ll_message_container) }
    private val mLlMessage by lazy { findViewById<LinearLayoutCompat>(R.id.fl_message) }
    private val mIvAvatarLeft by lazy { findViewById<ImageView>(R.id.iv_avatar_left) }
    private val mIvSendFailed by lazy { findViewById<ImageView>(R.id.iv_send_failed) }

    override fun onCreate(parent: ViewGroup) {
        setContentView(R.layout.item_chat_message)
    }

    override fun onBindData(data: ChatMessageViewData, position: Int) {
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
        mTvMsg.text = msg.content
        mLlMessageContainer.orientation =
            if (mTvMsg.width > (mLlMessageContainer.parent as ViewGroup).measuredWidth - 60) {
                LinearLayoutCompat.VERTICAL
            } else {
                LinearLayoutCompat.HORIZONTAL
            }

        if (data.unknown) {
            mTvMsg.text = "你收到了一条不支持的消息, 请更新客户端"
        }
        mLlMessageContainer.setOnLongClickListener {
            onLongClick(it, msg)
            return@setOnLongClickListener true
        }
    }
}