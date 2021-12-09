package pro.glideim.ui.chat

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.R
import pro.glideim.base.BaseActivity

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }

    private val mMessage = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mMessage)

    override val layoutResId = R.layout.activity_chat

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ChatActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {

        mMessage.add(MessageViewData().apply {
            this.avatar = ""
            this.content = "this is message."
            this.fromMe = false
            this.type = 1
            this.time = "10:19"
        })
        mMessage.add(MessageViewData().apply {
            this.avatar = ""
            this.content = "this is message2."
            this.fromMe = true
            this.type = 1
            this.time = "10:19"
        })
        mMessage.add(MessageViewData().apply {
            this.avatar = ""
            this.content = "this is message."
            this.fromMe = false
            this.type = 1
            this.time = "10:19"
        })

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
}