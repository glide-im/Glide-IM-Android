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
import pro.glideim.sdk.IMMessage
import pro.glideim.utils.loadImage
import pro.glideim.utils.secToTimeSpan

class ChatMessageViewHolder(v: ViewGroup) : AbsViewHolder<IMMessage>(v) {

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

    override fun onBindData(data: IMMessage, position: Int) {
        mIvSendFailed.gone()

        if (data.isMe) {
            when {
                data.isSendFailed -> {
                    mIvSendFailed.show()
                }
                data.isSending -> {
                    //mIvSendFailed.show()
                    mIvSendFailed.setImageTintColor(R.color.gray)
                }
                data.isSendSuccess -> {
                    //mIvSendFailed.show()
                    mIvSendFailed.setImageTintColor(R.color.primaryLightColor)
                }
            }
            mIvAvatarRight.loadImage(data.avatar)
            mIvAvatarRight.show()
            mIvAvatarLeft.hide()
            (mLlMessage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END
        } else {
            mIvAvatarLeft.loadImage(data.avatar)
            mIvAvatarLeft.show()
            mIvAvatarRight.hide()
            (mLlMessage.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
        }
        mTvTime.text = data.sendAt.secToTimeSpan()
        mTvMsg.text = data.content
        mLlMessageContainer.orientation =
            if (mTvMsg.width > (mLlMessageContainer.parent as ViewGroup).measuredWidth - 60) {
                LinearLayoutCompat.VERTICAL
            } else {
                LinearLayoutCompat.HORIZONTAL
            }
        mLlMessageContainer.setOnLongClickListener {
            onLongClick(it, data)
            return@setOnLongClickListener true
        }
    }
}