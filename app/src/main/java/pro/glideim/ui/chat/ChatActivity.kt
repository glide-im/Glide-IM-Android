package pro.glideim.ui.chat

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.intentExtra
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.api.msg.GetChatHistoryDto
import pro.glideim.sdk.api.msg.MsgApi
import pro.glideim.utils.request

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }

    private val mMessage = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mMessage)

    private val mUID by intentExtra(EXTRA_UID, 0L)

    override val layoutResId = R.layout.activity_chat

    companion object {

        const val EXTRA_UID = "EXTRA_UID"

        @JvmStatic
        fun start(context: Context, uid: Long) {
            val starter = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_UID, uid)
            }
            context.startActivity(starter)
        }
    }

    override fun initView() {

        mTvTitle.text = "Chat $mUID"

        mAdapter.addViewHolderForType(
            MessageViewData::class.java,
            ChatMessageViewHolder::class.java
        )
        mRvMessages.adapter = mAdapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        mRvMessages.layoutManager = layoutManager

        mBtSend.setOnClickListener {
            sendMessage()
        }
        requestData()
    }

    private fun sendMessage() {
        val msg = mEtMessage.text.toString()
        if (msg.isBlank()) {
            return
        }

        mMessage.add(MessageViewData().apply {
            this.avatar = ""
            this.content = msg
            this.fromMe = true
            this.type = 1
            this.time = "10:19"
        })
        mAdapter.notifyItemInserted(mMessage.size - 1)
        mEtMessage.setText("")
    }

    private fun requestData() {
        MsgApi.API.getChatMessageHistory(GetChatHistoryDto(mUID))
            .request(this) {
                mMessage.clear()
                mMessage.addAll(it!!.map {
                    MessageViewData().apply {
                        content = it.content
                        time = "00:00"
                        fromMe = GlideIM.getMyUID() == it.from
                    }
                })
            }
    }
}